package com.foreach.test.web.convertor;

import com.foreach.spring.enums.CodeLookup;
import com.foreach.spring.enums.IdLookup;
import com.foreach.web.convertor.EnumConverterFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;

public class TestConvertorFactory
{
	private enum Flavour implements IdLookup<Integer>
	{
		VANILLA( 1 ),
		STRAWBERRY( 2 );

		private int id;

		private Flavour( int id )
		{
			this.id = id;
		}

		public Integer getId()
		{
			return id;
		}
	}

	private enum Color implements CodeLookup<String>
	{
		RED( "R" ),
		GREEN( "G" ),
		BLUE( "B" );

		private String code;

		private Color( String code )
		{
			this.code = code;
		}

		public String getCode()
		{
			return code;
		}
	}

	private enum Gender implements CodeLookup<Character>
	{
		MALE( 'm' ),
		FEMALE( 'f' ),
		NEUTER( 'n' );

		private Character code;

		private Gender( Character code )
		{
			this.code = code;
		}

		public Character getCode()
		{
			return code;
		}
	}

	private class StringConversionService implements ConversionService
	{
		public boolean canConvert( Class<?> sourceType, Class<?> targetType )
		{
			return true;
		}

		public <T> T convert( Object source, Class<T> targetType )
		{
			if ( source instanceof Character ) {
				return (T) new Character( source.toString().charAt( 0 ) );
			}

			try {
				return (T) new Integer( Integer.parseInt( source.toString(), 10 ) );

			}
			catch ( NumberFormatException nfe ) {
				return null;
			}
		}

		public boolean canConvert( TypeDescriptor sourceType, TypeDescriptor targetType )
		{
			return true;
		}

		public Object convert( Object source, TypeDescriptor sourceType, TypeDescriptor targetType )
		{
			if ( targetType.getType().equals( Character.class ) ) {
				return source.toString().charAt( 0 );
			}

			try {
				return new Integer( Integer.parseInt( source.toString(), 10 ) );

			}
			catch ( NumberFormatException nfe ) {
				return null;
			}
		}
	}

	private EnumConverterFactory converterfactory;
	private ConversionService conversionService;

	@Before
	public void prepareForTest()
	{
		converterfactory = new EnumConverterFactory();

		conversionService = new StringConversionService();

		converterfactory.setConversionService( conversionService );
	}

	@Test
	public void convertToEnum()
	{

		Converter<String, Flavour> flavourConvertor = converterfactory.getConverter( Flavour.class );

		for ( Flavour flavour : Flavour.values() ) {
			Assert.assertEquals( flavour, flavourConvertor.convert( flavour.getId().toString() ) );
		}

		Converter<String, Color> colorConvertor = converterfactory.getConverter( Color.class );

		for ( Color color : Color.values() ) {
			Assert.assertEquals( color, colorConvertor.convert( color.getCode() ) );
		}

		Converter<String, Gender> genderConvertor = converterfactory.getConverter( Gender.class );

		for ( Gender gender : Gender.values() ) {
			Assert.assertEquals( gender, genderConvertor.convert( gender.getCode().toString() ) );
		}

	}
}
