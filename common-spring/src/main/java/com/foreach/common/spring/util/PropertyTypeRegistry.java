package com.foreach.common.spring.util;

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
		private final Object defaultValue;
		private final ConversionService conversionService;

		private PropertyTypeRecord( TypeDescriptor propertyType,
		                            Object defaultValue,
		                            ConversionService conversionService ) {
			this.propertyType = propertyType;
			this.defaultValue = defaultValue;
			this.conversionService = conversionService;
		}

		public TypeDescriptor getPropertyType() {
			return propertyType;
		}

		public Object getDefaultValue() {
			return defaultValue;
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

	public <A> void register( T propertyKey, Class<A> propertyClass, A propertyValue ) {
		register( propertyKey, TypeDescriptor.valueOf( propertyClass ), propertyValue, null );
	}

	public void register( T propertyKey, TypeDescriptor propertyType, Object propertyValue ) {
		definitions.put( propertyKey, new PropertyTypeRecord( propertyType, propertyValue, null ) );
	}

	public <A> void register( T propertyKey,
	                          Class<A> propertyClass,
	                          A propertyValue,
	                          ConversionService conversionService ) {
		register( propertyKey, TypeDescriptor.valueOf( propertyClass ), propertyValue, conversionService );
	}

	public void register( T propertyKey,
	                      TypeDescriptor propertyType,
	                      Object propertyValue,
	                      ConversionService conversionService ) {
		definitions.put( propertyKey, new PropertyTypeRecord( propertyType, propertyValue, conversionService ) );
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

	public Object getDefaultValueForProperty( T propertyKey ) {
		PropertyTypeRecord actual = definitions.get( propertyKey );

		if ( actual != null ) {
			return actual.getDefaultValue();
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
