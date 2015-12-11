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
package com.foreach.common.web.logging;

import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * Predicate that evaluates to true only if the argument is not assignable to any of the configured exceptions.
 *
 * @author pavan
 */
public class ExcludedExceptionPredicate implements ExceptionPredicate
{
	private Iterable<Class<? extends Exception>> exceptionClasses;

	public ExcludedExceptionPredicate( Iterable<Class<? extends Exception>> exceptionClasses ) {
		Assert.notNull( exceptionClasses );
		this.exceptionClasses = exceptionClasses;
	}

	public ExcludedExceptionPredicate( Class<? extends Exception>... exceptionClass ) {
		this.exceptionClasses = Arrays.asList( exceptionClass );
	}

	@Override
	public boolean evaluate( Exception exception ) {
		boolean result = true;
		for ( Class exceptionClass : this.exceptionClasses ) {
			if ( exceptionClass.isAssignableFrom( exception.getClass() ) ) {
				result = false;
			}
		}
		return result;
	}

}
