package com.foreach.common.spring.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation that validates an object using the AtLeastOneNotNullFieldValidator class.
 * <p/>
 * Example use:
 * <pre>
 * import com.foreach.spring.validators.AtLeastOneNotNullField;
 *
 * &#64;AtLeastOneNotNullField(fields = { "title", "code" })
 * public class Bar
 * {
 *
 *  private String title;
 *  private String code;
 *
 * </pre>
 */
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = AtLeastOneNotNullFieldValidator.class)
@Documented
public @interface AtLeastOneNotNullField
{
	String[] fields() default { };

	String message() default "{com.foreach.common.spring.validators.AtLeastOneNotNullField.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
