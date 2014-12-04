package com.foreach.common.spring.util;

import com.foreach.common.spring.convert.HierarchicalConversionService;
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
