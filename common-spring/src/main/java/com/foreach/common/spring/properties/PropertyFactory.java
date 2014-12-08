package com.foreach.common.spring.properties;

/**
 * Interface for creating property default values.
 *
 * @author Arne Vandamme
 * @see com.foreach.common.spring.properties.PropertyTypeRegistry
 */
public interface PropertyFactory<T, Y>
{
	/**
	 * Creates a new instance of the property.
	 *
	 * @param registry    Registry that is creating the default value.
	 * @param propertyKey Property key for which value is to be created.
	 * @return Instance.
	 */
	Y create( PropertyTypeRegistry registry, T propertyKey );
}
