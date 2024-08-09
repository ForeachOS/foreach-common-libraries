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

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

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
	String message() default "{com.foreach.common.spring.validators.ISBN.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
