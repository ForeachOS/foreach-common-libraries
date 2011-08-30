package com.foreach.spring.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A utility class for a Future that is always precomputed.
 * <p/>
 * A typical use for DummyFuture is when synchronously executing a Callable.
 */
public class DummyFuture<V> implements Future<V>
{
	private final V v;
	private final Exception e;

	public DummyFuture( V v, Exception e )
	{
		this.v = v;
		this.e = e;
	}

	/**
	 * @return false
	 */
	public final boolean cancel( boolean mayInterruptIfRunning )
	{
		return false;
	}

	/**
	 * @return false
	 */
	public final boolean isCancelled()
	{
		return false;
	}

	/**
	 * @return true
	 */
	public final boolean isDone()
	{
		return true;
	}

	public final V get() throws InterruptedException, ExecutionException
	{
		if( e!= null) {
			throw new ExecutionException( e );
		}

		return v;
	}

	public final V get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
	{
		return get();
	}

}
