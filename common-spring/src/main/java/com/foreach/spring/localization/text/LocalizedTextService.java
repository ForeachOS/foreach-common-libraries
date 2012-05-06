package com.foreach.spring.localization.text;


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
}
