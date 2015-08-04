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
package com.foreach.common.spring.properties;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Extends the TypedPropertyMap by caching the values instead of converting all the time.
 * Changes directly in the source map will not be detected unless an explicit {@link #refresh()} is done.
 * Changes through the CachingTypedPropertyMap should not be a problem.
 * </p>
 * <p><strong>Note:</strong> Only call for the registered type will be cached, that means that direct
 * calls to {@link #getValue(Object, Class)} will always use the conversionService.</p>
 *
 * @see com.foreach.common.spring.properties.TypedPropertyMap
 */
@SuppressWarnings("unchecked")
public class CachingTypedPropertyMap<T> extends TypedPropertyMap<T>
{
	private Map<T, Object> cachedValues = new HashMap<T, Object>();

	public CachingTypedPropertyMap( PropertyTypeRegistry<T> propertyTypeRegistry,
	                                Map<T, ?> source,
	                                Class sourceValueClass ) {
		super( propertyTypeRegistry, source, sourceValueClass );
	}

	@Override
	public <O> O getValue( T property ) {
		if ( !cachedValues.containsKey( property ) ) {
			Object value = super.getValue( property );
			cachedValues.put( property, value );
		}

		return (O) cachedValues.get( property );
	}

	@Override
	public Object set( T property, Object value ) {
		cachedValues.remove( property );
		return super.set( property, value );
	}

	@Override
	public void clear() {
		cachedValues.clear();
		super.clear();
	}

	@Override
	public Object remove( Object key ) {
		cachedValues.remove( key );
		return super.remove( key );
	}

	/**
	 * Clears all cached values, ensuring that on the next request the properties will be converted again.
	 */
	public void refresh() {
		cachedValues.clear();
	}

	/**
	 * Creates a duplicate of the TypedPropertyMap with its own property source, but the
	 * same registry and conversionService.
	 *
	 * @return Detached duplicate of the current map.
	 */
	@Override
	public TypedPropertyMap<T> detach() {
		Map<T, Object> sourceCopy = new HashMap<>();
		for ( Entry<T, Object> entry : entrySet() ) {
			sourceCopy.put( entry.getKey(), entry.getValue() );
		}

		return new CachingTypedPropertyMap<T>( propertyTypeRegistry, sourceCopy, sourceValueClass );
	}
}
