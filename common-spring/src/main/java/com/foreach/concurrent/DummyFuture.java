package com.foreach.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A utility class for a Future that is always precomputed
 * @param <V> the result type of the Future
 */

public class DummyFuture<V> implements Future<V>
{
	private V v;

	public DummyFuture( V v )
	{
		this.v = v;
	}

	public final boolean cancel(boolean mayInterruptIfRunning)
	{
		return false;
	}

	public final boolean isCancelled()
	{
		return false;
	}

	public final boolean isDone()
	{
		return true;
	}

	public final V get() throws InterruptedException, ExecutionException
	{
		return v;
	}

	public final V get(long timeout, TimeUnit unit)
	    throws InterruptedException, ExecutionException, TimeoutException
	{
		return v;
	}

}
