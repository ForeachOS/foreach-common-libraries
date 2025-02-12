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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * @author Arne Vandamme
 */
public class TestInstancePropertyFactory
{
	@Test
	public void createListInstances() {
		PropertyFactory<String, ArrayList> factory = InstancePropertyFactory.forClass( ArrayList.class );

		List one = factory.create( null, null );
		List two = factory.create( null, null );

		assertNotNull( one );
		assertNotNull( two );
		assertNotSame( one, two );
	}
}
