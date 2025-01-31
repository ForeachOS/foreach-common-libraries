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

import com.foreach.common.spring.enums.CodeLookup;
import com.foreach.common.spring.enums.IdLookup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestConvertorFactory
{
	private enum Flavour implements IdLookup<Integer>
	{
		VANILLA( 1 ),
		STRAWBERRY( 2 );

		private int id;

		private Flavour( int id ) {
			this.id = id;
		}

		public Integer getId() {
			return id;
		}
	}

	private enum Color implements CodeLookup<String>
	{
		RED( "R" ),
		GREEN( "G" ),
		BLUE( "B" );

		private String code;

		private Color( String code ) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}

	private enum Gender implements CodeLookup<Character>
	{
		MALE( 'm' ),
		FEMALE( 'f' ),
		NEUTER( 'n' );

		private Character code;

		private Gender( Character code ) {
			this.code = code;
		}

		public Character getCode() {
			return code;
		}
	}

	private class StringConversionService implements ConversionService
	{
		public boolean canConvert( Class<?> sourceType, Class<?> targetType ) {
			return true;
		}

		public <T> T convert( Object source, Class<T> targetType ) {
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

		public boolean canConvert( TypeDescriptor sourceType, TypeDescriptor targetType ) {
			return true;
		}

		public Object convert( Object source, TypeDescriptor sourceType, TypeDescriptor targetType ) {
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

	@BeforeEach
	public void prepareForTest() {
		converterfactory = new EnumConverterFactory();

		conversionService = new StringConversionService();

		converterfactory.setConversionService( conversionService );
	}

	@Test
	public void convertToEnum() {

		Converter<String, Flavour> flavourConvertor = converterfactory.getConverter( Flavour.class );

		for ( Flavour flavour : Flavour.values() ) {
			assertEquals( flavour, flavourConvertor.convert( flavour.getId().toString() ) );
		}

		Converter<String, Color> colorConvertor = converterfactory.getConverter( Color.class );

		for ( Color color : Color.values() ) {
			assertEquals( color, colorConvertor.convert( color.getCode() ) );
		}

		Converter<String, Gender> genderConvertor = converterfactory.getConverter( Gender.class );

		for ( Gender gender : Gender.values() ) {
			assertEquals( gender, genderConvertor.convert( gender.getCode().toString() ) );
		}

	}
}
