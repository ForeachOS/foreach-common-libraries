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
package com.foreach.common.test.web.logging;

import com.foreach.common.web.logging.IncludedExceptionPredicate;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author pavan
 */
public class TestIncludedExceptionPredicate
{
	@Test
	public void predicateEvaluateToTrueWithVarArgsConstructor() {
		IncludedExceptionPredicate predicate = new IncludedExceptionPredicate( RuntimeException.class );
		boolean result = predicate.evaluate( new ArrayIndexOutOfBoundsException() );
		assertTrue( result );
	}

	@Test
	public void predicateEvaluateToFalseWithVarArgsConstructor() {
		IncludedExceptionPredicate predicate = new IncludedExceptionPredicate( ArrayIndexOutOfBoundsException.class );
		boolean result = predicate.evaluate( new RuntimeException() );
		assertFalse( result );
	}

	@Test
	public void predicateEvaluateToTrueWithIterableConstructor() {
		List<Class<? extends Exception>> exceptions = new ArrayList<>();
		exceptions.add( RuntimeException.class );

		IncludedExceptionPredicate predicate = new IncludedExceptionPredicate( exceptions );
		boolean result = predicate.evaluate( new ArrayIndexOutOfBoundsException() );

		assertTrue( result );
	}

	@Test
	public void predicateEvaluateToFalseWithIterableConstructor() {
		List<Class<? extends Exception>> exceptions = new ArrayList<>();
		exceptions.add( ArrayIndexOutOfBoundsException.class );

		IncludedExceptionPredicate predicate = new IncludedExceptionPredicate( exceptions );
		boolean result = predicate.evaluate( new RuntimeException() );

		assertFalse( result );
	}
}
