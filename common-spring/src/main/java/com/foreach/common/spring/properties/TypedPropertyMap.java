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

import com.foreach.common.spring.properties.support.DirectPropertiesSource;
import org.springframework.core.convert.TypeDescriptor;

import java.util.*;

/**
 * <p>A TypedPropertyMap is proxy for a source list of key/value pairs, that allows getting and setting
 * of properties in a strongly typed fashion.  Usually the source is a simple map with both String key and value.</p>
 * <p>For this to be possible, the properties must be defined.  Meaning that every property key must
 * have an associated strong typed to map to.</p>
 * <p>Additionally a ConversionService must be present that can perform the type conversion for every property.</p>
 * <p><strong>Note:</strong> the standard implementation does not cache the fetched values and will always read from
 * the backing source directly.  See also {@link com.foreach.common.spring.properties.CachingTypedPropertyMap}.</p>
 * <p>This class also implements the Map interface to facilitate use in JSP/JSTL.  Note that the semantics for the
 * general Map interface might be somewhat dodgy and slower than a regular map.</p>
 *
 * @param <T> The type of the property key (most often String).
 * @see com.foreach.common.spring.properties.CachingTypedPropertyMap
 * @see com.foreach.common.spring.properties.PropertyTypeRegistry
 * @see com.foreach.common.spring.properties.PropertiesSource
 * @see org.springframework.core.convert.ConversionService
 */
@SuppressWarnings("unchecked")
public class TypedPropertyMap<T> implements Map<T, Object>, Cloneable
{
	protected final PropertyTypeRegistry<T> propertyTypeRegistry;
	protected final Class sourceValueClass;

	protected final PropertiesSource source;

	/**
	 * Construct a new TypedPropertyMap.
	 *
	 * @param propertyTypeRegistry Registry that contains the property keys with their corresponding type
	 *                             and conversion information.
	 * @param source               Backing source map containing the stored values.
	 * @param sourceValueClass     Class to use when setting values on the source map.
	 */
	public TypedPropertyMap( PropertyTypeRegistry<T> propertyTypeRegistry, Map<T, ?> source, Class sourceValueClass ) {
		this.propertyTypeRegistry = propertyTypeRegistry;
		this.source = new DirectPropertiesSource<T>( source );
		this.sourceValueClass = sourceValueClass;
	}

	/**
	 * @return The source backing this typed property map.
	 */
	public PropertiesSource getSource() {
		return source;
	}

	/**
	 * Construct a new TypedPropertyMap.
	 *
	 * @param propertyTypeRegistry Registry that contains the property keys with their corresponding type.
	 * @param source               Backing source proxy containing the stored values.
	 * @param sourceValueClass     Class to use when setting values on the source map.
	 * @see com.foreach.common.spring.properties.PropertiesSource
	 */
	public TypedPropertyMap( PropertyTypeRegistry<T> propertyTypeRegistry,
	                         PropertiesSource source, Class sourceValueClass ) {
		this.propertyTypeRegistry = propertyTypeRegistry;
		this.source = source;
		this.sourceValueClass = sourceValueClass;
	}

	/**
	 * Allows a strong typed property to be fetched from the source map.  This method will lookup the specific type
	 * of the property requested.  Use the conversionService to convert the source value to the typed value, and then
	 * case the result to the type requested.
	 *
	 * @param property Key of the property.
	 * @param <O>      Strong type value to return, the must registered type for the property must be able to cast to this type!
	 * @return Strong typed instance of the property.
	 */
	public <O> O getValue( T property ) {
		TypeDescriptor actualType = propertyTypeRegistry.getTypeForProperty( property );

		return (O) getValue( property, actualType );
	}

	/**
	 * Fetches a property from the source map and converts it to the type expected.  This circumvents the registry but
	 * forces a conversion to the requested target type.
	 *
	 * @param property     Key of the property.
	 * @param expectedType Type the value should be converted to and will be returned.
	 * @param <O>          Strong type value to return.
	 * @return Strong typed instance of the property.
	 */
	public <O> O getValue( T property, Class<O> expectedType ) {
		return getValue( property, TypeDescriptor.valueOf( expectedType ) );
	}

	/**
	 * Fetches a property from the source map and converts it to the type expected.  This circumvents the registry but
	 * forces a conversion to the requested target type.
	 *
	 * @param property     Key of the property.
	 * @param expectedType Type the value should be converted to and will be returned.
	 * @param <O>          Strong type value to return, converted value will be cast to the return type.
	 * @return Strong typed instance of the property.
	 */
	public <O> O getValue( T property, TypeDescriptor expectedType ) {
		Object originalValue;

		if ( source.getProperties().containsKey( property ) ) {
			originalValue = source.getProperties().get( property );
		}
		else {
			originalValue = propertyTypeRegistry.getDefaultValueForProperty( property );
			set( property, originalValue );
		}

		return (O) propertyTypeRegistry
				.getConversionServiceForProperty( property )
				.convert( originalValue, TypeDescriptor.forObject( originalValue ), expectedType );
	}

	/**
	 * Stores the property value in the source map by converting it to the sourceValueClass first.
	 *
	 * @param property Key of the property.
	 * @param value    Strong type value to set the property to.
	 */
	public Object set( T property, Object value ) {
		Object convertedValue = propertyTypeRegistry
				.getConversionServiceForProperty( property )
				.convert( value, sourceValueClass );

		return source.getProperties().put( property, convertedValue );
	}

	public int size() {
		return source.getProperties().size();
	}

	public boolean isEmpty() {
		return source.getProperties().isEmpty();
	}

	public boolean containsKey( Object key ) {
		return source.getProperties().containsKey( key );
	}

	public boolean containsValue( Object value ) {
		return values().contains( value );
	}

	@SuppressWarnings("unchecked")
	public Object get( Object key ) {
		return getValue( (T) key );
	}

	public Object put( T key, Object value ) {
		return set( key, value );
	}

	public Object remove( Object key ) {
		return source.getProperties().remove( key );
	}

	public void putAll( Map<? extends T, ?> m ) {
		for ( Entry<? extends T, ?> entry : m.entrySet() ) {
			put( entry.getKey(), entry.getValue() );
		}
	}

	public void clear() {
		source.getProperties().clear();
	}

	public Set<T> keySet() {
		Set<Entry<T, Object>> entries = entrySet();
		Set<T> list = new HashSet<T>();

		for ( Entry<T, Object> entry : entries ) {
			list.add( entry.getKey() );
		}

		return list;
	}

	public Collection<Object> values() {
		Set<Entry<T, Object>> entries = entrySet();
		Collection<Object> list = new ArrayList<>( entries.size() );

		for ( Entry<T, Object> entry : entries ) {
			list.add( entry.getValue() );
		}

		return list;
	}

	public Set<Entry<T, Object>> entrySet() {
		Set<Entry<T, Object>> entries = new HashSet<>();

		final TypedPropertyMap<T> myself = this;

		for ( final Object key : source.getProperties().keySet() ) {
			entries.add( new Entry<T, Object>()
			{
				public T getKey() {
					return (T) key;
				}

				public Object getValue() {
					return myself.getValue( (T) key );
				}

				public Object setValue( Object value ) {
					return myself.set( getKey(), value );
				}
			} );
		}

		return entries;
	}

	/**
	 * Creates a duplicate of the TypedPropertyMap with its own property source, but the
	 * same registry and conversionService.
	 *
	 * @return Detached duplicate of the current map.
	 */
	public TypedPropertyMap<T> detach() {
		Map<T, Object> sourceCopy = new HashMap<>();
		for ( Entry<T, Object> entry : entrySet() ) {
			sourceCopy.put( entry.getKey(), entry.getValue() );
		}

		return new TypedPropertyMap<>( propertyTypeRegistry, sourceCopy, sourceValueClass );
	}
}
