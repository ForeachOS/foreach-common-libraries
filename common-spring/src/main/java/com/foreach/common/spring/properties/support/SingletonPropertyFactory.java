package com.foreach.common.spring.properties.support;

import com.foreach.common.spring.properties.PropertyFactory;
import com.foreach.common.spring.properties.PropertyTypeRegistry;

/**
 * Always returns the same singleton value for a property.
 *
 * @author Arne Vandamme
 */
public class SingletonPropertyFactory<T, Y> implements PropertyFactory<T, Y>
{
	private final Y instance;

	public SingletonPropertyFactory( Y instance ) {
		this.instance = instance;
	}

	@Override
	public Y create( PropertyTypeRegistry registry, T propertyKey ) {
		return instance;
	}

	public static <A, B> PropertyFactory<A, B> forValue( B date ) {
		return new SingletonPropertyFactory<>( date );
	}
}
