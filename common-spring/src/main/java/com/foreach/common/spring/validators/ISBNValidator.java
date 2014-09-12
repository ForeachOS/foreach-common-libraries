package com.foreach.common.spring.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * IsbnValidator validates a string containing an ISBN number.
 * <p/>
 * ISBN consists of 4 groups of numbers separated by either dashes (-) or
 * spaces. The first group is 1-5 characters, second 1-7, third 1-6, and
 * fourth is 1 digit or an X.
 * The number should pass a mathematical calculation before being valid.
 * null strings are returned as valid.
 */
public class ISBNValidator implements ConstraintValidator<ISBN, String>
{
	private static org.apache.commons.validator.routines.ISBNValidator
			isbnValidator = org.apache.commons.validator.routines.ISBNValidator.getInstance();

	@Override
	public void initialize( ISBN constraintAnnotation ) {
	}

	/**
	 * @param value   the isbn string to be validated
	 * @param context ignored.
	 * @return true if the passed isbn is a valid ISBN number
	 */
	@Override
	public boolean isValid( String value, ConstraintValidatorContext context ) {
		return  value == null || isbnValidator.isValid( value );
	}
}
