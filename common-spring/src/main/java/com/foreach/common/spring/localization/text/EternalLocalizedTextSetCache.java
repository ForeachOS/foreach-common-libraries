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

import java.util.*;

/**
 * Default implementation of {@link LocalizedTextSetCache} that stores an infinite number of LocalizedTextSet
 * instances in a simple internal map structure, forever or until cleared.
 */
public class EternalLocalizedTextSetCache implements LocalizedTextSetCache
{
	private Map<String, LocalizedTextSet> cache =
			Collections.synchronizedMap( new HashMap<String, LocalizedTextSet>() );

	/**
	 * Gets a {@link LocalizedTextSet} for a group of items from the cache.
	 *
	 * @param application Application to get the group of items from.
	 * @param group       Name of the group.
	 * @return All items converted into a set instance.
	 */
	public final LocalizedTextSet getLocalizedTextSet( String application, String group ) {
		return cache.get( cacheKey( application, group ) );
	}

	/**
	 * Stores a LocalizedTextSet in the cache.
	 *
	 * @param textSet LocalizedTextSet to save.
	 */
	public final void storeLocalizedTextSet( LocalizedTextSet textSet ) {
		if ( textSet != null ) {
			cache.put( cacheKey( textSet.getApplication(), textSet.getGroup() ), textSet );
		}
	}

	private String cacheKey( String application, String group ) {
		return application + group;
	}

	/**
	 * @return The number of sets in the cache.
	 */
	public final int size() {
		return cache.size();
	}

	/**
	 * Clears the cache.
	 */
	public final void clear() {
		cache.clear();
	}

	/**
	 * Reloads all LocalizedTextSet instances kept in the cache.
	 */
	public final void reload() {
		for ( LocalizedTextSet set : new HashSet<LocalizedTextSet>( cache.values() ) ) {
			set.reload();
		}
	}

	/**
	 * Reloads the specific LocalizedTextSet instance if kept in the cache.
	 *
	 * @param application Application of the set of items.
	 * @param group       Group of the set of items.
	 */
	public final void reload( String application, String group ) {
		LocalizedTextSet cachedSet = getLocalizedTextSet( application, group );

		if ( cachedSet != null ) {
			cachedSet.reload();
		}
	}

	/**
	 * @return All LocalizedTextSet instances currently in the cache.
	 */
	public final Set<LocalizedTextSet> getCachedTextSets() {
		return new HashSet<LocalizedTextSet>( cache.values() );
	}
}
