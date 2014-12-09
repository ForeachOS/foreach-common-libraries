package com.foreach.common.spring.properties.support;

import com.foreach.common.spring.properties.PropertyFactory;
import com.foreach.common.spring.properties.PropertyTypeRegistry;

/**
 * Creates a new instance of a class, useful for creating new collections.
 *
 * @author Arne Vandamme
 */
public class InstancePropertyFactory<T, Y> implements PropertyFactory<T, Y>
{
	private final Class<Y> instanceClass;

	public InstancePropertyFactory( Class<Y> instanceClass ) {
		this.instanceClass = instanceClass;
	}

	@Override
	public Y create( PropertyTypeRegistry registry, T propertyKey ) {
		try {
			return instanceClass.newInstance();
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	public static <T, Y> InstancePropertyFactory<T, Y> forClass( Class<Y> clazz ) {
		return new InstancePropertyFactory<>( clazz );
	}
}
