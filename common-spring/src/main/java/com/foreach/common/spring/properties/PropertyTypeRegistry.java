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

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>Registry mapping properties by name to the type they are supposed to be.  Additionally
 * allowing for custom type conversion strategy to be defined.
 * If no property is defined with that name, the registry will return the default type.</p>
 * <p>A PropertyDefinitionRegistry is used as the configuration for a TypedPropertyMap.</p>
 */
public class PropertyTypeRegistry<T>
{
	private static class PropertyTypeRecord
	{
		private final TypeDescriptor propertyType;
		private final PropertyFactory defaultValueFactory;
		private final ConversionService conversionService;

		private PropertyTypeRecord( TypeDescriptor propertyType,
		                            PropertyFactory defaultValueFactory,
		                            ConversionService conversionService ) {
			this.propertyType = propertyType;
			this.defaultValueFactory = defaultValueFactory;
			this.conversionService = conversionService;
		}

		public TypeDescriptor getPropertyType() {
			return propertyType;
		}

		public PropertyFactory getDefaultValueFactory() {
			return defaultValueFactory;
		}

		public ConversionService getConversionService() {
			return conversionService;
		}
	}

	private final Map<T, PropertyTypeRecord> definitions = new TreeMap<>();

	private Class classForUnknownProperties = String.class;
	private ConversionService defaultConversionService;

	public PropertyTypeRegistry() {
	}

	public PropertyTypeRegistry( ConversionService conversionService ) {
		this( String.class, conversionService );
	}

	public PropertyTypeRegistry( Class classForUnknownProperties ) {
		this( classForUnknownProperties, null );
	}

	public PropertyTypeRegistry( Class classForUnknownProperties, ConversionService conversionService ) {
		this.classForUnknownProperties = classForUnknownProperties;

		setDefaultConversionService( conversionService );
	}

	public void setDefaultConversionService( ConversionService defaultConversionService ) {
		this.defaultConversionService = defaultConversionService;
	}

	public ConversionService getDefaultConversionService() {
		return defaultConversionService;
	}

	public void register( T propertyKey, Class propertyClass ) {
		register( propertyKey, propertyClass, null );
	}

	public void register( T propertyKey, TypeDescriptor propertyType ) {
		register( propertyKey, propertyType, null );
	}

	public <A> void register( T propertyKey, Class<A> propertyClass, PropertyFactory<T, A> defaultValueFactory ) {
		register( propertyKey, TypeDescriptor.valueOf( propertyClass ), defaultValueFactory, null );
	}

	public void register( T propertyKey, TypeDescriptor propertyType, PropertyFactory<T, ?> defaultValueFactory ) {
		definitions.put( propertyKey, new PropertyTypeRecord( propertyType, defaultValueFactory, null ) );
	}

	public <A> void register( T propertyKey,
	                          Class<A> propertyClass,
	                          PropertyFactory<T, A> defaultValueFactory,
	                          ConversionService conversionService ) {
		register( propertyKey, TypeDescriptor.valueOf( propertyClass ), defaultValueFactory, conversionService );
	}

	public void register( T propertyKey,
	                      TypeDescriptor propertyType,
	                      PropertyFactory<T, ?> defaultValueFactory,
	                      ConversionService conversionService ) {
		definitions.put( propertyKey, new PropertyTypeRecord( propertyType, defaultValueFactory, conversionService ) );
	}

	public void unregister( T propertyKey ) {
		definitions.remove( propertyKey );
	}

	public TypeDescriptor getTypeForProperty( T propertyKey ) {
		PropertyTypeRecord actual = definitions.get( propertyKey );

		if ( actual != null ) {
			return actual.getPropertyType();
		}

		return TypeDescriptor.valueOf( getClassForUnknownProperties() );
	}

	public Class getClassForProperty( T propertyKey ) {
		PropertyTypeRecord actual = definitions.get( propertyKey );

		if ( actual != null ) {
			return actual.getPropertyType().getType();
		}

		return getClassForUnknownProperties();
	}

	@SuppressWarnings("unchecked")
	public Object getDefaultValueForProperty( T propertyKey ) {
		PropertyTypeRecord actual = definitions.get( propertyKey );

		if ( actual != null ) {
			PropertyFactory factory = actual.getDefaultValueFactory();

			if ( factory != null ) {
				return factory.create( this, propertyKey );
			}
		}

		return null;
	}

	/**
	 * @return The actual ConversionService that should be used for handling the property.
	 */
	public ConversionService getConversionServiceForProperty( T propertyKey ) {
		PropertyTypeRecord actual = definitions.get( propertyKey );

		ConversionService conversionService = null;

		if ( actual != null ) {
			conversionService = actual.getConversionService();
		}

		return conversionService != null ? conversionService : defaultConversionService;
	}

	public Class getClassForUnknownProperties() {
		if ( classForUnknownProperties == null ) {
			throw new RuntimeException( "No class registered for unknown properties." );
		}
		return classForUnknownProperties;
	}

	public void setClassForUnknownProperties( Class classForUnknownProperties ) {
		this.classForUnknownProperties = classForUnknownProperties;
	}

	public boolean isRegistered( T propertyKey ) {
		return definitions.containsKey( propertyKey );
	}

	public Collection<T> getRegisteredProperties() {
		return definitions.keySet();
	}

	public boolean isEmpty() {
		return definitions.isEmpty();
	}

	public void clear() {
		definitions.clear();
	}
}
