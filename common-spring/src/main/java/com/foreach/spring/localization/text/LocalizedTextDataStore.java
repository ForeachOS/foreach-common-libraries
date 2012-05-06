package com.foreach.spring.localization.text;

import java.util.List;

/**
 * An interface to communicate between a {@link LocalizedTextService} and a backing datastore containing the items.
 * A bean implementing this interface should be passed to the {@link AbstractLocalizedTextService} implementation.
 */
public interface LocalizedTextDataStore
{
	/**
	 * Gets a list of all LocalizedText items for a given group.
	 *
	 * @param application Application to get the group of items from.
	 * @param group       Name of the group.
	 * @return List of items.
	 */
	List<LocalizedText> getLocalizedTextForGroup( String application, String group );

	/**
	 * Gets an item with a specified label from a particular group.
	 *
	 * @param application Application the item belongs to.
	 * @param group       Name of the group the item belongs to.
	 * @param label       Label of the item, unique within the group.
	 * @return Item or null if not found.
	 */
	LocalizedText getLocalizedText( String application, String group, String label );

	/**
	 * Inserts a new item in the datastore.
	 *
	 * @param text LocalizedText item.
	 */
	void insertLocalizedText( LocalizedText text );

	/**
	 * Updates an existing item in the datastore.
	 *
	 * @param text LocalizedText item.
	 */
	void updateLocalizedText( LocalizedText text );

	/**
	 * Deletes an item in the datastore.
	 *
	 * @param text LocalizedText item.
	 */
	void deleteLocalizedText( LocalizedText text );

	/**
	 * @return All applications in the datastore.
	 */
	List<String> getApplications();

	/**
	 * @param application Name of an application in the datastore.
	 * @return List of groups for this application.
	 */
	List<String> getGroups( String application );

	/**
	 * Flags an item as used in the datastore.
	 *
	 * @param text LocalizedText item to be flagged.
	 */
	void flagAsUsed( LocalizedText text );

	/**
	 * This will do a search over all items in the datastore, looking for the text specified.
	 *
	 * @param textToSearch Text to search for.
	 * @return All items that have a match on the text.
	 */
	List<LocalizedText> searchLocalizedText( String textToSearch );
}
