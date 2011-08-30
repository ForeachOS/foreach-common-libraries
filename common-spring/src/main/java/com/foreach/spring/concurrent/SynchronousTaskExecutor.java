package com.foreach.spring.concurrent;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * SynchronousTaskExecutor is a partial synchronous implementation of ExecutorService.
 * <p/>
 * On methods with timeout, the timout values are ignored.
 */
public class SynchronousTaskExecutor implements ExecutorService
{
	private static final Logger LOG = Logger.getLogger( SynchronousTaskExecutor.class );

	private volatile boolean stopped = false;

	public final synchronized void shutdown()
	{
		stopped = true;
	}

	public final List<Runnable> shutdownNow()
	{
		shutdown();
		return new ArrayList<Runnable>();
	}

	public final synchronized boolean isShutdown()
	{
		return stopped;
	}

	public final boolean isTerminated()
	{
		return isShutdown();
	}

	public final boolean awaitTermination(long timeout, TimeUnit unit)
	    throws InterruptedException
	{
		return true;
	}

	private void checkNotStopped()
	{
		if(isShutdown())
			throw new RejectedExecutionException();
	}

	public final void execute(Runnable command)
	{
		checkNotStopped();
		command.run();
	}

	public final <T> Future<T> submit(Callable<T> task)
	{
		checkNotStopped();

		try {
			return new DummyFuture<T>( task.call(), null );
		} catch ( Exception e ) {
			return new DummyFuture<T>( null, e );
		}
	}

	public final <T> Future<T> submit(Runnable task, T result)
	{
		try {
			task.run();
			return new DummyFuture<T>( result, null );
		} catch ( Exception e ) {
			return new DummyFuture<T>( null, e );
		}
	}

	public final Future<?> submit(Runnable task)
	{
		return submit( task, null );
	}

	public final <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
	    throws InterruptedException
	{
		List<Future<T>> result = new ArrayList<Future<T>>();

		for(Callable<T> task : tasks) {
			result.add( submit( task ) );
		}

		return result;
	}

	public final <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
	                              long timeout, TimeUnit unit)
	    throws InterruptedException
	{
		return invokeAll( tasks );
	}

	public final <T> T invokeAny(Collection<? extends Callable<T>> tasks)
	    throws InterruptedException, ExecutionException
	{
		for(Callable<T> task : tasks) {
			try {
				return task.call();
			} catch (Exception e) {

			}
		}

		throw new ExecutionException( "No task completed succesfully", null );
	}

	public final <T> T invokeAny(Collection<? extends Callable<T>> tasks,
	                long timeout, TimeUnit unit)
	    throws InterruptedException, ExecutionException, TimeoutException
	{
		return invokeAny( tasks );
	}
}
