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
	 * @param group       Group of the set of items.
	 */
	void reload( String application, String group );

	/**
	 * @return All LocalizedTextSet instances currently in the cache.
	 */
	Set<LocalizedTextSet> getCachedTextSets();
}
