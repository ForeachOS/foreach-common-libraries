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

import com.foreach.common.spring.properties.support.SingletonPropertyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestTypedPropertyMap.Config.class)
@DirtiesContext
public class TestCachingTypedPropertyMap extends TestTypedPropertyMap
{
	@Override
	protected TypedPropertyMap<String> createMap() {
		return new CachingTypedPropertyMap<>( registry, source, String.class );
	}

	@Override
	public void valueChangedDirectlyInSourceMap() {
		// In this case the old value should be returned
		registry.register( "number", Integer.class );

		Integer number = map.getValue( "number" );
		assertEquals( 1981561, number.intValue() );
		assertEquals( number, map.get( "number" ) );

		source.remove( "number" );
		number = map.getValue( "number" );
		assertEquals( 1981561, number.intValue() );
		assertEquals( number, map.get( "number" ) );

		source.put( "number", "100" );
		number = map.getValue( "number" );
		assertEquals( 1981561, number.intValue() );
		assertEquals( number, map.get( "number" ) );
	}

	@Test
	public void aRefreshClearsTheCache() {
		registry.register( "number", Integer.class );

		Integer number = map.getValue( "number" );
		assertEquals( 1981561, number.intValue() );
		assertEquals( number, map.get( "number" ) );

		source.remove( "number" );
		number = map.getValue( "number" );
		assertEquals( 1981561, number.intValue() );
		assertEquals( number, map.get( "number" ) );

		( (CachingTypedPropertyMap) map ).refresh();
		assertNull( map.getValue( "number" ) );

		source.put( "number", "100" );
		assertNull( map.getValue( "number" ) );
		( (CachingTypedPropertyMap) map ).refresh();

		number = map.getValue( "number" );
		assertEquals( 100, number.intValue() );
		assertEquals( number, map.get( "number" ) );
	}

	@Override
	public void genericLists() {
		registry.register( "dates",
		                   TypeDescriptor.collection( List.class, TypeDescriptor.valueOf( Date.class ) ),
		                   SingletonPropertyFactory.<String, List>forValue( Collections.<Date>emptyList() )
		);

		assertEquals( Collections.<Date>emptyList(), map.get( "dates" ) );

		Date dateOne = new Date( 1286683200000L );
		Date dateTwo = new Date( 1280683200000L );

		map.put( "dates", Arrays.asList( dateOne, dateTwo ) );

		assertEquals( "1286683200000,1280683200000", source.get( "dates" ) );

		source.put( "dates", "1286683200000" );
		assertEquals( Collections.singletonList( dateOne ), map.getValue( "dates" ) );

		map.clear();
		source.put( "dates", "1280683200000,1286683200000" );
		List<Date> dates = map.getValue( "dates" );
		assertEquals( Arrays.asList( dateTwo, dateOne ), dates );

		Set<Date> dateSet =
				map.getValue( "dates", TypeDescriptor.collection( Set.class, TypeDescriptor.valueOf( Date.class ) ) );
		assertEquals( 2, dateSet.size() );
		assertTrue( dateSet.containsAll( Arrays.asList( dateOne, dateTwo ) ) );
	}

	@Override
	public void defaultValueIsSetButDetachedAfterGet() {
		registry.register( "myprop", Set.class, ( registry, propertyKey ) -> new HashSet() );

		TypedPropertyMap<String> detached = map.detach();

		Set<String> one = map.getValue( "myprop" );
		assertNotNull( one );

		one.add( "somestring" );

		assertTrue( source.containsKey( "myprop" ) );

		// Because caching map is used, same instance of the set will be fetched
		Set<String> oneAgain = map.getValue( "myprop" );
		assertSame( one, oneAgain );

		Set<String> two = detached.getValue( "myprop" );
		assertNotNull( two );
		assertNotSame( one, two );
		assertTrue( two.isEmpty() );
	}
}
