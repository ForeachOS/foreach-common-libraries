package com.foreach.validators;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.impl.EmailValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

/**
 * MultipleEmailsValidator validates a string containing multiple comma or semi-colon separated email
 * adresses.
 * <p/>
 * Because the individual email adresses are checked using the Hibernate EmailValidator,
 * there is no 100% compliance with RFC 2822.
 */
public class MultipleEmailsValidator implements ConstraintValidator<MultipleEmails, String>
{
	public void initialize( MultipleEmails multipleEmails )
	{
	}

	/**
	 * @param emailString                the string to be validated
	 * @param constraintValidatorContext ignored.
	 * @return true if the passed emailString is a comma or semi-colon separated list of valid email adresses.
	 */
	public final boolean isValid( String emailString, ConstraintValidatorContext constraintValidatorContext )
	{
		EmailValidator emailValidator = new EmailValidator();

		List<String> emails = separateEmailAddresses( emailString );

		for ( String email : emails ) {
			if ( !emailValidator.isValid( email, constraintValidatorContext ) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param emailString a string of comma or semicolon separated emailadresses.
	 * @return a list of the individual emailadresses in the argument passed.
	 */
	public static List<String> separateEmailAddresses( String emailString )
	{
		String[] notCleaned = StringUtils.split( emailString, ";," );

		List<String> cleaned = new ArrayList<String>();

		for ( String s : notCleaned ) {
			if ( !StringUtils.isBlank( s ) ) {
				cleaned.add( StringUtils.trim( s ) );
			}
		}

		return cleaned;
	}
}
