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
	 * Flags an item as used in the datastore.
	 *
	 * @param text LocalizedText item to be flagged.
	 */
	void flagAsUsed( LocalizedText text );
}
