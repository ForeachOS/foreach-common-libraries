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

import com.foreach.common.spring.convert.HierarchicalConversionService;
import com.foreach.common.spring.properties.support.SingletonPropertyFactory;
import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestTypedPropertyMap.Config.class)
@DirtiesContext
public class TestTypedPropertyMap
{
	@Autowired
	protected ConversionService conversionService;

	protected PropertyTypeRegistry<String> registry;
	protected Map<String, String> source;
	protected TypedPropertyMap<String> map;

	@Before
	public void reset() {
		registry = new PropertyTypeRegistry<>( conversionService );

		source = new HashMap<>();
		source.put( "date", "345790800000" );
		source.put( "decimal", "128.50" );
		source.put( "number", "1981561" );
		source.put( "text", "some text" );

		map = createMap();
	}

	protected TypedPropertyMap<String> createMap() {
		return new TypedPropertyMap<>( registry, source, String.class );
	}

	@Test
	public void propertyValuesReturnedAsStringIfNotSpecified() {
		assertEquals( "345790800000", map.getValue( "date" ) );
		assertEquals( "128.50", map.getValue( "decimal" ) );
		assertEquals( "1981561", map.getValue( "number" ) );
		assertEquals( "some text", map.getValue( "text" ) );
	}

	@Test
	public void ifNoDefaultValuesSpecifiedNullIsReturned() {
		map.clear();

		assertNull( map.getValue( "date" ) );
		assertNull( map.getValue( "decimal" ) );
		assertNull( map.getValue( "number" ) );
		assertNull( map.getValue( "text" ) );
	}

	@Test
	public void defaultValueAreReturnedIfValueNotSet() {
		map.clear();

		registry.register( "date", Date.class, SingletonPropertyFactory.<String, Date>forValue( new Date( 0 ) ) );
		registry.register( "decimal", BigDecimal.class,
		                   SingletonPropertyFactory.<String, BigDecimal>forValue( new BigDecimal( 10 ) )
		);
		registry.register( "number", Integer.class,
		                   SingletonPropertyFactory.<String, Integer>forValue( -1 )
		);
		registry.register( "text", String.class, SingletonPropertyFactory.<String, String>forValue( "" ) );

		assertEquals( new Date( 0 ), map.getValue( "date" ) );
		assertEquals( new BigDecimal( 10 ), map.getValue( "decimal" ) );
		assertEquals( -1, map.getValue( "number" ) );
		assertEquals( "", map.getValue( "text" ) );
	}

	@Test
	public void propertyValuesReturnedAsTypeRegistered() {
		registry.register( "date", Date.class );
		registry.register( "decimal", BigDecimal.class );
		registry.register( "number", Integer.class );
		registry.register( "text", String.class );

		Date date = map.getValue( "date" );
		assertEquals( "1980-12-16", new SimpleDateFormat( "yyyy-MM-dd" ).format( date ) );
		assertEquals( date, map.get( "date" ) );

		BigDecimal decimal = map.getValue( "decimal" );
		assertEquals( new BigDecimal( "128.50" ), decimal );
		assertEquals( decimal, map.get( "decimal" ) );

		Integer number = map.getValue( "number" );
		assertEquals( 1981561, number.intValue() );
		assertEquals( number, map.get( "number" ) );

		String text = map.getValue( "text" );
		assertEquals( "some text", text );
		assertEquals( text, map.get( "text" ) );
	}

	@Test
	public void propertyValuesConvertedOnGet() {
		registry.register( "date", Date.class );
		registry.register( "decimal", BigDecimal.class );
		registry.register( "number", Integer.class );

		assertEquals( Long.valueOf( "345790800000" ), map.getValue( "date", Long.class ) );
		assertEquals( Double.valueOf( "128.50" ), map.getValue( "decimal", Double.class ) );
		assertEquals( "1981561", map.getValue( "number", String.class ) );
	}

	@Test
	public void propertyValuesAreSentToSourceAndAvailableOnRead() {
		registry.register( "date", Date.class );
		registry.register( "decimal", BigDecimal.class );
		registry.register( "number", Integer.class );
		registry.register( "text", String.class );

		map.put( "decimal", new BigDecimal( "333.33" ) );
		map.put( "number", 987654321 );
		map.put( "text", null );
		map.put( "date", new Date( 1286683200000L ) );

		assertEquals( "333.33", source.get( "decimal" ) );
		assertEquals( "987654321", source.get( "number" ) );
		assertEquals( null, source.get( "text" ) );
		assertEquals( "1286683200000", source.get( "date" ) );

		Date date = map.getValue( "date" );
		assertEquals( 1286683200000L, date.getTime() );
		assertEquals( date, map.get( "date" ) );

		BigDecimal decimal = map.getValue( "decimal" );
		assertEquals( new BigDecimal( "333.33" ), decimal );
		assertEquals( decimal, map.get( "decimal" ) );

		Integer number = map.getValue( "number" );
		assertEquals( 987654321, number.intValue() );
		assertEquals( number, map.get( "number" ) );

		String text = map.getValue( "text" );
		assertNull( text );
		assertNull( map.get( "text" ) );
	}

	@Test
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

		source.put( "dates", "1280683200000,1286683200000" );
		List<Date> dates = map.getValue( "dates" );
		assertEquals( Arrays.asList( dateTwo, dateOne ), dates );

		Set<Date> dateSet =
				map.getValue( "dates", TypeDescriptor.collection( Set.class, TypeDescriptor.valueOf( Date.class ) ) );
		assertEquals( 2, dateSet.size() );
		assertTrue( dateSet.containsAll( Arrays.asList( dateOne, dateTwo ) ) );
	}

	@Test
	public void customConversionServices() {
		final Date other = new Date( 1 );

		GenericConversionService formattedConversionService = new GenericConversionService();
		formattedConversionService.addConverter( Date.class, String.class, new Converter<Date, String>()
		                                         {
			                                         @Override
			                                         public String convert( Date source ) {
				                                         return FastDateFormat.getInstance( "yyyyMMdd" ).format(
						                                         source );
			                                         }
		                                         }

		);

		HierarchicalConversionService hierarchicalConversionService = new HierarchicalConversionService(
				registry.getDefaultConversionService()
		);
		hierarchicalConversionService.addConverter( String.class, Date.class, new Converter<String, Date>()
		                                            {
			                                            @Override
			                                            public Date convert( String source ) {
				                                            return other;
			                                            }
		                                            }

		);

		registry.register( "timestamp", Date.class );
		registry.register( "formatted", Date.class, null, formattedConversionService );
		registry.register( "inherited", Date.class, null, hierarchicalConversionService );

		Date dateOne = new Date( 1286683200000L );
		String timestamp = "1286683200000";
		String formatted = FastDateFormat.getInstance( "yyyyMMdd" ).format( dateOne );

		map.put( "timestamp", dateOne );
		map.put( "formatted", dateOne );
		map.put( "inherited", dateOne );

		assertEquals( source.get( "timestamp" ), timestamp );
		assertEquals( source.get( "formatted" ), formatted );
		assertEquals( source.get( "inherited" ), timestamp );

		assertEquals( dateOne, map.get( "timestamp" ) );
		assertEquals( other, map.get( "inherited" ) );
	}

	@Test
	public void valueChangedDirectlyInSourceMap() {
		registry.register( "number", Integer.class );

		Integer number = map.getValue( "number" );
		assertEquals( 1981561, number.intValue() );
		assertEquals( number, map.get( "number" ) );

		source.remove( "number" );
		assertNull( map.getValue( "number" ) );

		source.put( "number", "100" );

		number = map.getValue( "number" );
		assertEquals( 100, number.intValue() );
		assertEquals( number, map.get( "number" ) );
	}

	@Test
	public void detachedCopyHasAnotherSource() {
		assertSame( source, map.getSource().getProperties() );

		TypedPropertyMap<String> detached = map.detach();
		assertSame( source, map.getSource().getProperties() );
		assertNotSame( source, detached.getSource().getProperties() );

		assertEquals( source, map.getSource().getProperties() );
		assertEquals( source, detached.getSource().getProperties() );
	}

	@Test
	public void defaultValueIsSetButDetachedAfterGet() {
		registry.register( "myprop", Set.class, new PropertyFactory<String, Set>()
		{
			@Override
			public Set create( PropertyTypeRegistry registry, String propertyKey ) {
				return new HashSet();
			}
		} );

		TypedPropertyMap<String> detached = map.detach();

		Set<String> one = map.getValue( "myprop" );
		assertNotNull( one );

		one.add( "somestring" );

		assertTrue( source.containsKey( "myprop" ) );

		// Even though the same map, no set() has been called so value from source is used
		Set<String> oneAgain = map.getValue( "myprop" );
		assertNotSame( one, oneAgain );
		assertTrue( oneAgain.isEmpty() );

		Set<String> two = detached.getValue( "myprop" );
		assertNotNull( two );
		assertNotSame( one, two );
		assertTrue( two.isEmpty() );
	}

	@Configuration
	public static class Config
	{
		@Bean
		public ConversionService conversionService() {
			DefaultConversionService service = new DefaultConversionService();
			service.addConverter( new Converter<String, Date>()
			{
				public Date convert( String source ) {
					return new Date( Long.valueOf( source ) );
				}
			} );

			service.addConverter( new Converter<Date, String>()
			{
				public String convert( Date source ) {
					return "" + source.getTime();
				}
			} );
			return service;
		}
	}
}
