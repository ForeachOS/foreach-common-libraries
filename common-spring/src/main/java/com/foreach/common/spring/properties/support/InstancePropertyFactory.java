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
