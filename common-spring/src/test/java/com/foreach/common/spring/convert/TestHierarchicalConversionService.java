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
package com.foreach.common.spring.convert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 */
public class TestHierarchicalConversionService
{
	private ConversionService parent;
	private HierarchicalConversionService local;

	@Before
	public void createHierarchy() {
		parent = mock( ConversionService.class );
		local = new HierarchicalConversionService( parent );
	}

	@Test
	public void canConvert() {
		when( parent.canConvert( TypeDescriptor.valueOf( String.class ), TypeDescriptor.valueOf( Long.class ) ) )
				.thenReturn( true );

		local.addConverter( String.class, Double.class, mock( Converter.class ) );

		assertFalse( local.canConvert( String.class, Integer.class ) );
		verify( parent ).canConvert( TypeDescriptor.valueOf( String.class ), TypeDescriptor.valueOf( Integer.class ) );

		assertTrue( local.canConvert( String.class, Long.class ) );

		assertTrue( local.canConvert( String.class, Double.class ) );
		verify( parent, never() ).canConvert( TypeDescriptor.valueOf( String.class ),
		                                      TypeDescriptor.valueOf( Double.class ) );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void localConversion() {
		Converter<Double, String> converter = mock( Converter.class );
		when( converter.convert( (double) 15 ) ).thenReturn( "local" );

		local.addConverter( Double.class, String.class, converter );

		String value = local.convert( (double) 15, String.class );
		assertEquals( "local", value );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void parentConversion() {
		when( parent.convert( (long) 20,
		                      TypeDescriptor.valueOf( Long.class ),
		                      TypeDescriptor.valueOf( String.class ) ) )
				.thenReturn( "parent" );

		String value = local.convert( (long) 20, String.class );
		assertEquals( "parent", value );
	}
}
