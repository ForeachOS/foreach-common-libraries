package com.foreach.common.spring.localization.text;

import java.util.List;

public interface LocalizedTextService
{
	/**
	 * Gets a {@link LocalizedTextSetImpl} for a group of items.
	 *
	 * @param application Application to get the group of items from.
	 * @param group       Name of the group.
	 * @return All items converted into a set instance.
	 */
	LocalizedTextSet getLocalizedTextSet( String application, String group );

	/**
	 * Gets a list of all LocalizedText items for a given group.
	 *
	 * @param application Application to get the group of items from.
	 * @param group       Name of the group.
	 * @return List of items.
	 */
	List<LocalizedText> getLocalizedTextItems( String application, String group );

	/**
	 * Gets a list of all LocalizedText items containing a string.
	 *
	 * @param textToSearchFor String with the text to search for.
	 * @return List of items.
	 */
	List<LocalizedText> searchLocalizedTextItemsForText( String textToSearchFor );

	/**
	 * Flags a text item as used, this will also call a method on the DAO to flag the item in the data store.
	 *
	 * @param text Text item that should be flagged as used.
	 */
	void flagAsUsed( LocalizedText text );

	/**
	 * <p>Creates a new text item with default values.  This will also execute an insert call on the DAO.
	 * Note that no exceptions are being thrown in case saving fails.</p>
	 *
	 * @param application  Application in which to create the text item.
	 * @param group        Group the text item should belong to.
	 * @param label        Label of the text item.
	 * @param defaultValue Default value to be set for the text.
	 * @return Constructed and saved text item.
	 */
	LocalizedText saveDefaultText( String application, String group, String label, String defaultValue );

	/**
	 * Gets a single LocalizedText item from the datastore.
	 *
	 * @param application Application in which to find the text item.
	 * @param group       Group the text item belongs to.
	 * @param label       Label of the text item.
	 * @return Text item or null if not found.
	 */
	LocalizedText getLocalizedText( String application, String group, String label );

	/**
	 * Saves a LocalizedText item in the backing datastore.  This will also trigger a reload of the cache for the
	 * set this item belongs to.
	 *
	 * @param text Text item to save.
	 */
	void saveLocalizedText( LocalizedText text );

	/**
	 * Deletes a LocalizedText item from the backing datastore.  This will also trigger a reload of the cache for
	 * the set this items belongs to.
	 *
	 * @param text Text item to delete.
	 */
	void deleteLocalizedText( LocalizedText text );

	/**
	 * @return All applications with text items..
	 */
	List<String> getApplications();

	/**
	 * @param application Name of an application.
	 * @return List of text item groups for this application.
	 */
	List<String> getGroups( String application );
}
