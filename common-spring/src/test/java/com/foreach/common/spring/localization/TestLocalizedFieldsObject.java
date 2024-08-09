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
package com.foreach.common.spring.localization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestLocalizedFieldsObject extends AbstractLocalizationTest
{
	private MyLocalizedText text;
	private Collection<MyFields> collection;
	private Map<String, MyFields> map;

	@BeforeEach
	public void createInstance() {
		text = new MyLocalizedText();

		collection = text.getFieldsAsCollection();
		map = text.getFields();
	}

	@Test
	public void setAndGetFieldsWithDefaults() {
		MyLocalizedTextWithDefaults text = new MyLocalizedTextWithDefaults();

		Map<String, MyFields> allFields = text.getFields();

		assertEquals( 2, allFields.size() );

		MyFields fields = text.getFieldsForLanguage( MyLanguage.EN );
		assertNotNull( fields );
		fields.setText( "Text in English 1" );

		validateSameInAllCollections( fields, text, MyLanguage.EN );

		fields = text.getFieldsForLanguage( MyLanguage.FR );
		assertNotNull( fields );
		fields.setText( "Texte en Français" );

		validateSameInAllCollections( fields, text, MyLanguage.FR );

		MyFields newFields = text.createFields( MyLanguage.EN );
		newFields.setText( "Text in English 2" );
		text.addFields( newFields );

		assertEquals( 2, allFields.size() );

		validateSameInAllCollections( newFields, text, MyLanguage.EN );
	}

	@Test
	public void verifyListCollection() {
		MyLocalizedTextWithDefaults text = new MyLocalizedTextWithDefaults();

		Collection<MyFields> fields = text.getFieldsAsCollection();
		assertNotNull( fields );
		assertEquals( 2, fields.size() );

		text.getFieldsForLanguage( MyLanguage.EN ).setText( "first" );

		for ( MyFields f : fields ) {
			if ( f.getLanguage() == MyLanguage.EN ) {
				assertEquals( "first", f.getText() );
			}
		}

		MyFields other = new MyFields( MyLanguage.EN );
		other.setText( "other" );
		fields.add( other );

		assertEquals( "other", text.getFieldsForLanguage( MyLanguage.EN ).getText() );
		assertEquals( 2, fields.size() );

		for ( MyFields f : fields ) {
			if ( f.getLanguage() == MyLanguage.EN ) {
				assertEquals( "other", f.getText() );
			}
		}
	}

	@Test
	public void removingFields() {
		MyLocalizedTextWithDefaults text = new MyLocalizedTextWithDefaults();

		Collection<MyFields> collection = text.getFieldsAsCollection();
		Map<String, MyFields> map = text.getFields();

		assertEquals( 2, collection.size() );
		assertEquals( 2, map.size() );

		text.removeFields( MyLanguage.EN );

		assertEquals( 1, collection.size() );
		assertEquals( 1, map.size() );
		assertFalse( map.containsKey( MyLanguage.EN.getCode() ) );
		assertTrue( map.containsKey( MyLanguage.FR.getCode() ) );
	}

	@Test
	public void addingFieldsThroughGet() {
		MyFields fields = text.getFieldsForLanguage( MyLanguage.EN );
		assertNotNull( fields );

		assertEquals( 2, collection.size() );
		assertEquals( 2, map.size() );

		validateSameInAllCollections( fields, text, MyLanguage.EN );
	}

	@Test
	public void addingFieldsThroughCollection() {
		MyFields fields = text.createFields( MyLanguage.EN );
		collection.add( fields );

		assertEquals( 2, collection.size() );
		assertEquals( 2, map.size() );

		validateSameInAllCollections( fields, text, MyLanguage.EN );
	}

	@Test
	public void addingFieldsExplicitly() {
		MyFields fields = text.createFields( MyLanguage.EN );
		text.addFields( fields );

		assertEquals( 2, collection.size() );
		assertEquals( 2, map.size() );

		validateSameInAllCollections( fields, text, MyLanguage.EN );
	}

	@Test
	public void settingAllFieldsAsCollection() {
		MyFields fieldsOne = text.createFields( MyLanguage.EN );
		MyFields fieldsTwo = text.createFields( MyLanguage.FR );
		MyFields fieldsThree = text.createFields( MyLanguage.EN );

		text.setFieldsAsCollection( Arrays.asList( fieldsOne, fieldsTwo, fieldsThree ) );

		assertEquals( 2, collection.size() );
		assertEquals( 2, map.size() );

		validateSameInAllCollections( fieldsTwo, text, MyLanguage.FR );
		validateSameInAllCollections( fieldsThree, text, MyLanguage.EN );
	}

	@Test
	public void clearingCollection() {
		MyLocalizedTextWithDefaults text = new MyLocalizedTextWithDefaults();

		Collection<MyFields> collection = text.getFieldsAsCollection();
		Map<String, MyFields> map = text.getFields();

		assertEquals( 2, collection.size() );
		assertEquals( 2, map.size() );

		collection.clear();
		assertTrue( collection.isEmpty() );
		assertTrue( map.isEmpty() );
	}

	private void validateSameInAllCollections( MyFields expected, MyLocalizedText text, MyLanguage language ) {
		MyFields fields = text.getFieldsForLanguage( language );
		assertSame( expected, fields );

		Collection<MyFields> collection = text.getFieldsAsCollection();
		assertTrue( collection.contains( fields ) );

		Map<String, MyFields> map = text.getFields();
		assertTrue( map.containsValue( fields ) );
		assertEquals( fields, map.get( language.getCode() ) );
	}

	class MyFields extends BaseLocalizedFields
	{
		private String text;

		public MyFields( Language language ) {
			super( language );
		}

		public String getText() {
			return text;
		}

		public void setText( String text ) {
			this.text = text;
		}
	}

	class MyLocalizedText extends AbstractLocalizedFieldsObject<MyFields>
	{
		@Override
		public MyFields createFields( Language language ) {
			return new MyFields( language );
		}
	}

	class MyLocalizedTextWithDefaults extends MyLocalizedText
	{
	}
}
