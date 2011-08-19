package com.foreach.validators;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.impl.EmailValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class MultipleEmailsValidator implements ConstraintValidator<MultipleEmails, String>
{
	public void initialize( MultipleEmails multipleEmails )
	{
	}

	public final boolean isValid( String emailString, ConstraintValidatorContext constraintValidatorContext )
	{
		EmailValidator emailValidator = new EmailValidator();

		List<String> emails = separateEmailAddresses(emailString);

		for ( String email : emails ) {
			if ( !emailValidator.isValid( email, constraintValidatorContext ) ) {
				return false;
			}
		}
		return true;
	}

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
