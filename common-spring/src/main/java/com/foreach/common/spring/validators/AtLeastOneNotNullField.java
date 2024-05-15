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
