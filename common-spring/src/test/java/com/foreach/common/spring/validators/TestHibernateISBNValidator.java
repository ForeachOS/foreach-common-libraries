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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestHibernateISBNValidator
{
	private ISBNValidator ISBNValidator = new ISBNValidator();

	@Test
	public void isValidWithNullStringReturnsTrue() throws Exception {
		assertTrue( ISBNValidator.isValid( null, null ) );
	}

	@Test
	public void isValidWithEmptyStringReturnsFalse() throws Exception {
		assertFalse( ISBNValidator.isValid( "", null ) );
	}

	@Test
	public void isValidWithInvalidIsbnReturnsFalse() throws Exception {
		assertFalse( ISBNValidator.isValid( "nope", null ) );
	}

	@Test
	public void isValidWithValidIsbnReturnsTrue() throws Exception {
		assertTrue( ISBNValidator.isValid( "978-3-16-148410-0", null ) );
	}
}
