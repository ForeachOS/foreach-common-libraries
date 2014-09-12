package com.foreach.common.spring.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * An annotation that validates ISBN numbers using the ISBNValidator class.
 * <p/>
 * Example use:
 * <pre>
 * import com.foreach.spring.validators.ISBN;
 *
 * public class Bar
 * {
 *
 * ...
 *
 *  &#64;NotBlank &#64;Length(max = 13, min = 10) &#64;ISBN
 *  private String isbn;
 *
 * </pre>
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ISBNValidator.class)
@Documented
public @interface ISBN
{
	String message() default "{ISBN}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
