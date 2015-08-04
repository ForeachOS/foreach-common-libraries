/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.common.spring.localization.text;

import com.foreach.common.spring.localization.Language;

import java.util.List;
import java.util.Map;

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

	/**
	 * <p>Converts all LocalizedTexts in the provided language to a Map.  The LocalizedTexts to be converted can be
	 * found in the given LocalizedTextSet.</p>
	 *
	 * @param localizedTextSet The LocalizedTextSet to convert.
	 * @param language         The language specifying which LocalizedTexts should be converted.
	 * @return The Map with the LocalizedTexts.  The keys of the map are the labels of the LocalizedText and the value is
	 * the translation of that label in the given Language.
	 */
	Map<String, String> getLanguageMap( LocalizedTextSet localizedTextSet, Language language );
}
