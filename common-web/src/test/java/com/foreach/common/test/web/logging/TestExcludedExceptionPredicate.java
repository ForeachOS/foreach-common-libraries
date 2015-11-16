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

import com.foreach.common.web.logging.ExcludedExceptionPredicate;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pavan
 */
public class TestExcludedExceptionPredicate
{
	@Test
	public void predicateEvaluateToTrueWithVarArgsConstructor(){
		ExcludedExceptionPredicate predicate = new ExcludedExceptionPredicate( ArrayIndexOutOfBoundsException.class );
		boolean result = predicate.evaluate( new RuntimeException() );
		Assert.assertEquals( true, result );
	}

	@Test
	public void predicateEvaluateToFalseWithVarArgsConstructor(){
		ExcludedExceptionPredicate predicate = new ExcludedExceptionPredicate( RuntimeException.class );
		boolean result = predicate.evaluate( new ArrayIndexOutOfBoundsException() );
		Assert.assertEquals( false, result );
	}

	@Test
	public void predicateEvaluateToTrueWithIterableConstructor(){
		List<Class<? extends Exception>> exceptions = new ArrayList<>();
		exceptions.add( ArrayIndexOutOfBoundsException.class );

		ExcludedExceptionPredicate predicate = new ExcludedExceptionPredicate( exceptions );
		boolean result = predicate.evaluate( new RuntimeException() );

		Assert.assertEquals( true, result );
	}

	@Test
	public void predicateEvaluateToFalseWithIterableConstructor(){
		List<Class<? extends Exception>> exceptions = new ArrayList<>();
		exceptions.add( RuntimeException.class );

		ExcludedExceptionPredicate predicate = new ExcludedExceptionPredicate( exceptions );
		boolean result = predicate.evaluate( new ArrayIndexOutOfBoundsException() );

		Assert.assertEquals( false, result );
	}
}
