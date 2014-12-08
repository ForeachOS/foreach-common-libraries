package com.foreach.common.spring.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
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
		                   Collections.<Date>emptyList()
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

}
