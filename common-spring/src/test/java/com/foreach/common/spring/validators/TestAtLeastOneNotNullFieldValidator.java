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
package com.foreach.common.spring.validators;


import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class TestAtLeastOneNotNullFieldValidator
{
	private AtLeastOneNotNullFieldValidator validator;

	@BeforeEach
	public void setUp() throws Exception {
		validator = new AtLeastOneNotNullFieldValidator();
	}

	@Test
	public void isValidWithNullObjectReturnsFalse() throws Exception {
		assertFalse( validator.isValid( null, null ) );
	}

	@Test
	public void isValidWithEmptyFieldListReturnsFalse() throws Exception {
		TestObjectEmptyFieldList testObject = new TestObjectEmptyFieldList();
		AtLeastOneNotNullField annotation = (AtLeastOneNotNullField) testObject.getClass().getDeclaredAnnotations()[0];
		validator.initialize( annotation );
		assertFalse( validator.isValid( testObject, null ) );
	}

	@Test
	public void isValidWithFieldListAndEmptyFieldsReturnsFalse() throws Exception {
		TestObject testObject = new TestObject();
		AtLeastOneNotNullField annotation = (AtLeastOneNotNullField) testObject.getClass().getDeclaredAnnotations()[0];
		validator.initialize( annotation );
		assertFalse( validator.isValid( testObject, null ) );
	}

	@Test
	public void isValidWithMisconfigurationThrowsException() throws Exception {
		TestObjectInvalidFieldList testObject = new TestObjectInvalidFieldList();
		AtLeastOneNotNullField annotation = (AtLeastOneNotNullField) testObject.getClass().getDeclaredAnnotations()[0];
		validator.initialize( annotation );
		assertThrows( RuntimeException.class, () ->validator.isValid( testObject, null ));
	}

	@Test
	public void isValidWithFieldListAndAtLeastOneFilledInFieldReturnsTrue() throws Exception {
		TestObjectWithMultipleFields testObject = new TestObjectWithMultipleFields();
		testObject.setTitle( "filledIn" );
		AtLeastOneNotNullField annotation = (AtLeastOneNotNullField) testObject.getClass().getDeclaredAnnotations()[0];
		validator.initialize( annotation );
		assertTrue( validator.isValid( testObject, null ) );
	}

	@Test
	public void isValidWithFieldListAndFilledInCollectionReturnsTrue() throws Exception {
		TestObjectWithCollection testObject = new TestObjectWithCollection();
		Collection<String> titles = new ArrayList<>();
		titles.add( "filledIn" );
		testObject.setTitles( titles );
		AtLeastOneNotNullField annotation = (AtLeastOneNotNullField) testObject.getClass().getDeclaredAnnotations()[0];
		validator.initialize( annotation );
		assertTrue( validator.isValid( testObject, null ) );
	}

	@AtLeastOneNotNullField
	public class TestObjectEmptyFieldList
	{
		private String title;

		public String getTitle() {
			return title;
		}

		public void setTitle( String title ) {
			this.title = title;
		}
	}

	@AtLeastOneNotNullField(fields = "doesnotexist")
	public class TestObjectInvalidFieldList
	{
		private String title;

		public String getTitle() {
			return title;
		}

		public void setTitle( String title ) {
			this.title = title;
		}
	}

	@AtLeastOneNotNullField(fields = { "title" })
	public class TestObject
	{
		private String title;

		public String getTitle() {
			return title;
		}

		public void setTitle( String title ) {
			this.title = title;
		}
	}

	@AtLeastOneNotNullField(fields = { "title", "code" })
	public class TestObjectWithMultipleFields
	{
		private String title;
		private String code;

		public String getTitle() {
			return title;
		}

		public void setTitle( String title ) {
			this.title = title;
		}

		public String getCode() {
			return code;
		}

		public void setCode( String code ) {
			this.code = code;
		}
	}

	@AtLeastOneNotNullField(fields = { "titles" })
	public class TestObjectWithCollection
	{
		private Collection<String> titles;

		public Collection<String> getTitles() {
			return titles;
		}

		public void setTitles( Collection<String> titles ) {
			this.titles = titles;
		}
	}
}
