package com.foreach.common.spring.localization.text;

import com.foreach.common.concurrent.SynchronousTaskExecutor;
import com.foreach.common.concurrent.locks.ObjectLock;
import com.foreach.common.concurrent.locks.ObjectLockRepository;
import com.foreach.common.concurrent.locks.ReentrantObjectLockRepository;
import com.foreach.common.spring.localization.Language;
import com.foreach.common.spring.localization.LanguageConfigurator;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * <p>
 * The AbstractLocalizedTextService is a basic implementation of the {@link LocalizedTextService} interface,
 * providing all methods for lookup and creation of text items.  It has a tight integration with the
 * {@link LocalizedTextSetImpl} which provides an interface for quick text lookup in a collection of items.
 * </p>
 * <p>
 * The service requires a {@link LocalizedTextDataStore} implementation for access to the data store.  There
 * are also two optional properties: flaggingExecutorService and localizedTextSetCache.
 * </p>
 * <p>
 * The {@link #executorService} is the {@link java.util.concurrent.ExecutorService} that can be used to execute calls
 * in the background.  Calls to {@link #flagAsUsed(LocalizedText)}, {@link #saveLocalizedText(LocalizedText)} and
 * {@link #deleteLocalizedText(LocalizedText)} will put additional calls on the ExecutorService (cache reloads and
 * background updates).  If no executorService is specified, all calls are executed synchronously.
 * </p>
 * <p>
 * The {@link #textSetCache} is the {@link LocalizedTextSetCache} implementation that is used to cache all
 * fetched LocalizedTextSet instances.  Newly fetched instances will be stored in the cache and if an instance
 * is found in the cache it will be returned directly without DAO interaction.
 * See the {@link EternalLocalizedTextSetCache} for an implementation with eternal caching.
 * By default no caching is being done.
 * </p>
 */
public abstract class AbstractLocalizedTextService implements LocalizedTextService
{
	@SuppressWarnings("all")
	protected final Logger LOG;

	private final ObjectLockRepository<String> textSetFetchLocks = new ReentrantObjectLockRepository<String>();
	private final LocalizedTextDataStore localizedTextDao;

	private ExecutorService executorService = new SynchronousTaskExecutor();
	private LocalizedTextSetCache textSetCache = new NoCachingLocalizedTextSetCache();

	/**
	 * @param localizedTextDao DAO providing callback methods to the datastore.
	 */
	protected AbstractLocalizedTextService( LocalizedTextDataStore localizedTextDao ) {
		this.localizedTextDao = localizedTextDao;

		LOG = Logger.getLogger( this.getClass() );
	}

	/**
	 * @param executorService The ExecutorService to use for the {@link #flagAsUsed(LocalizedText)} calls.
	 */
	public final void setExecutorService( ExecutorService executorService ) {
		this.executorService = executorService;

		if ( this.executorService == null ) {
			this.executorService = new SynchronousTaskExecutor();
		}
	}

	/**
	 * @param textSetCache The LocalizedTextSetCache implementation to use for caching the textSet instances.
	 */
	public final void setTextSetCache( LocalizedTextSetCache textSetCache ) {
		this.textSetCache = textSetCache;

		if ( this.textSetCache == null ) {
			this.textSetCache = new NoCachingLocalizedTextSetCache();
		}
	}

	/**
	 * <p>Gets a {@link LocalizedTextSetImpl} for a group of items.  Synchronizes access to the same application/group combination.</p>
	 * <p>If the textSet is not found in the cache it will be fetched from the dataStore and stored in the
	 * cache.  While this is happening, other threads trying to access the same textSet will wait.
	 * </p>
	 *
	 * @param application Application to get the group of items from.
	 * @param group       Name of the group.
	 * @return All items converted into a set instance.
	 */
	public final LocalizedTextSet getLocalizedTextSet( String application, String group ) {
		LocalizedTextSet textSet = textSetCache.getLocalizedTextSet( application, group );

		if ( textSet == null ) {
			ObjectLock<String> writeLock = textSetFetchLocks.getLock( application + group );
			try {
				writeLock.lock();

				// Fetch again from cache to avoid double querying
				textSet = textSetCache.getLocalizedTextSet( application, group );

				if ( textSet == null ) {
					textSet = new LocalizedTextSetImpl( application, group, this );
					textSetCache.storeLocalizedTextSet( textSet );
				}

			}
			finally {
				writeLock.unlock();
			}
		}

		return textSet;
	}

	/**
	 * Gets a list of all LocalizedText items for a given group.
	 *
	 * @param application Application to get the group of items from.
	 * @param group       Name of the group.
	 * @return List of items.
	 */
	public final List<LocalizedText> getLocalizedTextItems( String application, String group ) {
		return localizedTextDao.getLocalizedTextForGroup( application, group );
	}

	/**
	 * Flags a text item as used, this will also call a method on the DAO to flag the item in the data store.
	 *
	 * @param text Text item that should be flagged as used.
	 */
	public final void flagAsUsed( final LocalizedText text ) {
		text.setUsed( true );
		text.setUpdated( new Date() );

		executorService.submit( new Runnable()
		{
			public void run() {
				try {
					localizedTextDao.flagAsUsed( text );
				}
				catch ( RuntimeException re ) {
					LOG.error( "Failed to flag item as used: " + text, re );
				}
			}
		} );
	}

	/**
	 * <p>Creates a new text item with default values.  If the item already exists in the datastore, the existing
	 * item will be returned instead.  Otherwise this will also execute an insert call on the DAO.
	 * Note that no exceptions are being thrown in case saving fails.  An ERROR message will be logged
	 * but the constructed text item will be returned.</p>
	 * <p>Unlike a normal save or delete, this method will not trigger a cache reload for the text set
	 * the item belongs to.  Because this method does a lookup first, it is assumed that other instances
	 * will fetch the recently created item and return it instead of trying to save twice.  In that case
	 * fetching the single item is cheaper than reloading the entire set.</p>
	 *
	 * @param application  Application in which to create the text item.
	 * @param group        Group the text item should belong to.
	 * @param label        Label of the text item.
	 * @param defaultValue Default value to be set for the text.
	 * @return Constructed and saved text item.
	 */
	public final synchronized LocalizedText saveDefaultText( String application,
	                                                         String group,
	                                                         String label,
	                                                         String defaultValue ) {
		// Look directly in the database to see if the text item does not yet exist
		LocalizedText existing = localizedTextDao.getLocalizedText( application, group, label );

		if ( existing != null ) {
			LOG.debug( "Not creating text item with defaults because it already exists: " + existing );
			return existing;
		}

		LocalizedText text = new LocalizedText();
		text.setApplication( application );
		text.setGroup( group );
		text.setLabel( label );

		for ( Language language : LanguageConfigurator.getLanguages() ) {
			text.getFieldsForLanguage( language ).setText( defaultValue );
		}

		text.setUsed( true );
		text.setAutoGenerated( true );
		text.setCreated( new Date() );

		LOG.debug( "Creating new text item with defaults: " + text );

		try {
			localizedTextDao.insertLocalizedText( text );
		}
		catch ( RuntimeException re ) {
			LOG.error( "Insert of new text item failed ", re );
		}

		return text;
	}

	/**
	 * Gets a single LocalizedText item from the datastore.
	 *
	 * @param application Application in which to find the text item.
	 * @param group       Group the text item belongs to.
	 * @param label       Label of the text item.
	 * @return Text item or null if not found.
	 */
	public final LocalizedText getLocalizedText( String application, String group, String label ) {
		return localizedTextDao.getLocalizedText( application, group, label );
	}

	/**
	 * Saves a LocalizedText item in the backing datastore.  This will also trigger a reload of the cache for the
	 * set this item belongs to.
	 *
	 * @param text Text item to save.
	 */
	public final void saveLocalizedText( final LocalizedText text ) {
		if ( text != null ) {
			LocalizedText existing = getLocalizedText( text.getApplication(), text.getGroup(), text.getLabel() );

			if ( existing == null ) {
				localizedTextDao.insertLocalizedText( text );
			}
			else {
				localizedTextDao.updateLocalizedText( text );
			}

			// Reload cache asynchronously
			executorService.submit( new Runnable()
			{
				public void run() {
					try {
						textSetCache.reload( text.getApplication(), text.getGroup() );
					}
					catch ( RuntimeException re ) {
						LOG.error( "Failed to reload cache ", re );
					}
				}
			} );
		}
	}

	/**
	 * Gets a list of all LocalizedText items containing a string.
	 *
	 * @param textToSearchFor String with the text to search for.
	 * @return List of items.
	 */
	public final List<LocalizedText> searchLocalizedTextItemsForText( String textToSearchFor ) {
		return localizedTextDao.searchLocalizedText( textToSearchFor );
	}

	/**
	 * @return All applications with text items.
	 */
	public final List<String> getApplications() {
		return localizedTextDao.getApplications();
	}

	/**
	 * @param application Name of an application.
	 * @return List of text item groups for this application.
	 */
	public final List<String> getGroups( String application ) {
		return localizedTextDao.getGroups( application );
	}

	/**
	 * Deletes a LocalizedText item from the backing datastore.  This will also trigger a reload of the cache for
	 * the set this items belongs to.
	 *
	 * @param text Text item to delete.
	 */
	public final void deleteLocalizedText( final LocalizedText text ) {
		if ( text != null ) {
			localizedTextDao.deleteLocalizedText( text );

			// Reload cache asynchronously
			executorService.submit( new Runnable()
			{
				public void run() {
					try {
						textSetCache.reload( text.getApplication(), text.getGroup() );
					}
					catch ( RuntimeException re ) {
						LOG.error( "Failed to reload cache ", re );
					}
				}
			} );
		}
	}

	private static final class NoCachingLocalizedTextSetCache implements LocalizedTextSetCache
	{
		public LocalizedTextSet getLocalizedTextSet( String application, String group ) {
			return null;
		}

		public void storeLocalizedTextSet( LocalizedTextSet textSet ) {
		}

		public int size() {
			return 0;
		}

		public void clear() {
		}

		public void reload() {
		}

		public void reload( String application, String group ) {
		}

		public Set<LocalizedTextSet> getCachedTextSets() {
			return Collections.emptySet();
		}
	}
}
