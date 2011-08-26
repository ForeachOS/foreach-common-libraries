package com.foreach.spring.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Target( { METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MultipleEmailsValidator.class)
@Documented
public @interface MultipleEmails
{
	String message() default "{MultipleEmails}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
