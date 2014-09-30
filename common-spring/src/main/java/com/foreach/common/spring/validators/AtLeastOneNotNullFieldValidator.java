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
