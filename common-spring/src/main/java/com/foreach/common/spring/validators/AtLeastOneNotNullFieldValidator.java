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

import org.apache.commons.beanutils.BeanUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * AtLeastOneNotNullFieldValidator validates an object whether it has at least one
 * of the listed properties filled in (as in, not null).
 * <p/>
 * Note: a null object or empty collections will return false
 */
public class AtLeastOneNotNullFieldValidator implements ConstraintValidator<AtLeastOneNotNullField, Object>
{
	private String[] fieldNames;

	@Override
	public void initialize( AtLeastOneNotNullField constraintAnnotation ) {
		fieldNames = constraintAnnotation.fields();
	}

	@Override
	public boolean isValid( Object value, ConstraintValidatorContext context ) {
		if ( value == null || fieldNames == null || fieldNames.length == 0 ) {
			return false;
		}
		boolean isValid = false;
		List<String> fieldNameList = Arrays.asList( fieldNames );
		Iterator<String> fieldNameIterator = fieldNameList.iterator();
		while ( !isValid && fieldNameIterator.hasNext() ) {
			String fieldName = fieldNameIterator.next();
			try {
				String property = BeanUtils.getProperty( value, fieldName );
				if ( property != null ) {
					isValid = true;
				}
			}
			catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException e ) {
				throw new RuntimeException( e );
			}
		}
		return isValid;
	}
}
