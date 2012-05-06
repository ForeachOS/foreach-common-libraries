package com.foreach.spring.localization.text;

import java.util.Set;

public interface LocalizedTextSetCache
{
	/**
	 * Gets a {@link LocalizedTextSet} for a group of items from the cache.
	 *
	 * @param application Application to get the group of items from.
	 * @param group       Name of the group.
	 * @return All items converted into a set instance.
	 */
	LocalizedTextSet getLocalizedTextSet( String application, String group );

	/**
	 * Stores a LocalizedTextSet in the cache.
	 *
	 * @param textSet LocalizedTextSet to save.
	 */
	void storeLocalizedTextSet( LocalizedTextSet textSet );

	/**
	 * @return The number of items in the cache.
	 */
	int size();

	/**
	 * Clears the cache.
	 */
	void clear();

	/**
	 * Reloads all LocalizedTextSet instances kept in the cache.
	 */
	void reload();

	/**
	 * Reloads the specific LocalizedTextSet instance if kept in the cache.
	 *
	 * @param application Application of the set of items.
	 * @param group Group of the set of items.
	 */
	void reload( String application, String group );

	/**
	 * @return All LocalizedTextSet instances currently in the cache.
	 */
	Set<LocalizedTextSet> getCachedTextSets();
}
