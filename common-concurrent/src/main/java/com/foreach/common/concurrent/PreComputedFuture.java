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
package com.foreach.common.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A utility class for a Future that is always precomputed.
 * <p/>
 * A typical use for PreComputedFuture is when synchronously executing a Callable.
 */
public class PreComputedFuture<V> implements Future<V>
{
	private final V v;
	private final Exception e;

	/**
	 * @param v the result to be returned by get, provided e is null.
	 */
	public PreComputedFuture( V v ) {
		this( v, null );
	}

	/**
	 * @param v the result to be returned by get, provided e is null.
	 * @param e if not null, this exception will be wrapped in an ExecutionException
	 *          and thrown on any call to a get(...) method.
	 */
	public PreComputedFuture( V v, Exception e ) {
		this.v = v;
		this.e = e;
	}

	/**
	 * @return false
	 */
	public final boolean cancel( boolean mayInterruptIfRunning ) {
		return false;
	}

	/**
	 * @return false
	 */
	public final boolean isCancelled() {
		return false;
	}

	/**
	 * @return true
	 */
	public final boolean isDone() {
		return true;
	}

	/**
	 * If the instance was created with a non-null exception argument,
	 * wraps it in an ExecutionException and returns it,
	 * otherwise it returns the result.
	 *
	 * @throws ExecutionException
	 */
	public final V get() throws InterruptedException, ExecutionException {
		if ( e != null ) {
			throw new ExecutionException( e );
		}

		return v;
	}

	/**
	 * Because the result is precomputed, this routine behaves as get(),
	 * the timeout argument is ignored.
	 */
	public final V get( long timeout,
	                    TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
		return get();
	}

}
