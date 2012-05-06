package com.foreach.spring.localization.text;

import com.foreach.spring.concurrent.SynchronousTaskExecutor;
import com.foreach.spring.localization.Language;
import com.foreach.spring.localization.LanguageConfigurator;
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
 * The {@link #flaggingExecutorService} is the {@link java.util.concurrent.ExecutorService} that will be used to execute all
 * calls to {@link #flagAsUsed(LocalizedText)}.  This can be a good approach to
 * update items as used in the data store in an asynchronous manner.  If no flaggingExecutorService is specified,
 * all calls are executed synchronously.
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
	protected final Logger LOG;

	private final LocalizedTextDataStore localizedTextDao;

	private ExecutorService flaggingExecutorService = new SynchronousTaskExecutor();
	private LocalizedTextSetCache textSetCache = new NoCachingLocalizedTextSetCache();

	/**
	 * @param localizedTextDao DAO providing callback methods to the datastore.
	 */
	protected AbstractLocalizedTextService( LocalizedTextDataStore localizedTextDao )
	{
		this.localizedTextDao = localizedTextDao;

		LOG = Logger.getLogger( this.getClass() );
	}

	/**
	 * @param flaggingExecutorService The ExecutorService to use for the {@link #flagAsUsed(LocalizedText)} calls.
	 */
	public final void setFlaggingExecutorService( ExecutorService flaggingExecutorService )
	{
		this.flaggingExecutorService = flaggingExecutorService;

		if ( this.flaggingExecutorService == null ) {
			this.flaggingExecutorService = new SynchronousTaskExecutor();
		}
	}

	/**
	 * @param textSetCache The LocalizedTextSetCache implementation to use for caching the textSet instances.
	 */
	public void setTextSetCache( LocalizedTextSetCache textSetCache )
	{
		this.textSetCache = textSetCache;

		if ( this.textSetCache == null ) {
			this.textSetCache = new NoCachingLocalizedTextSetCache();
		}
	}

	/**
	 * Gets a {@link LocalizedTextSetImpl} for a group of items.
	 *
	 * @param application Application to get the group of items from.
	 * @param group       Name of the group.
	 * @return All items converted into a set instance.
	 */
	public final LocalizedTextSet getLocalizedTextSet( String application, String group )
	{
		LocalizedTextSet textSet = textSetCache.getLocalizedTextSet( application, group );

		if ( textSet == null ) {
			textSet = new LocalizedTextSetImpl( application, group, this );
			textSetCache.storeLocalizedTextSet( textSet );
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
	public List<LocalizedText> getLocalizedTextItems( String application, String group )
	{
		return localizedTextDao.getLocalizedTextForGroup( application, group );
	}

	/**
	 * Flags a text item as used, this will also call a method on the DAO to flag the item in the data store.
	 *
	 * @param text Text item that should be flagged as used.
	 */
	public final void flagAsUsed( final LocalizedText text )
	{
		text.setUsed( true );
		text.setUpdated( new Date() );

		flaggingExecutorService.submit( new Runnable()
		{
			public void run()
			{
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
	 * <p>Creates a new text item with default values.  This will also execute an insert call on the DAO.
	 * Note that no exceptions are being thrown in case saving fails.  An ERROR message will be logged
	 * but the constructed text item will be returned.</p>
	 *
	 * @param application  Application in which to create the text item.
	 * @param group        Group the text item should belong to.
	 * @param label        Label of the text item.
	 * @param defaultValue Default value to be set for the text.
	 * @return Constructed and saved text item.
	 */
	public final synchronized LocalizedText saveDefaultText(
			String application, String group, String label, String defaultValue )
	{
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

	private static final class NoCachingLocalizedTextSetCache implements LocalizedTextSetCache
	{
		public LocalizedTextSet getLocalizedTextSet( String application, String group )
		{
			return null;
		}

		public void storeLocalizedTextSet( LocalizedTextSet textSet )
		{
		}

		public int size()
		{
			return 0;
		}

		public void clear()
		{
		}

		public void reload()
		{
		}

		public Set<LocalizedTextSet> getCachedTextSets()
		{
			return Collections.emptySet();
		}
	}
}
