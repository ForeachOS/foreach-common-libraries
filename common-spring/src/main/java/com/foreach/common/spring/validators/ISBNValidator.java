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
