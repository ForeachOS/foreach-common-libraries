package com.foreach.common.spring.validators;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestISBNValidator
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
