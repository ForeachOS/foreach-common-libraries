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
