package com.foreach.common.spring.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * An annotation that validates using the MultipleEmailsValidator class.
 * <p/>
 * Example use:
 * <pre>
 * import com.foreach.spring.validators.MultipleEmails;
 *
 * public class Bar
 * {
 *
 * ...
 *
 *  &#64;NotBlank &#64;Length(max = 200) &#64;MultipleEmails
 *  private String email;
 *
 * </pre>
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MultipleEmailsValidator.class)
@Documented
public @interface MultipleEmails
{
	String message() default "{com.foreach.common.spring.validators.MultipleEmails.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
