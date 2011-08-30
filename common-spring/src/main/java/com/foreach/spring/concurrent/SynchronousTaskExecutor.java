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

	public synchronized void shutdown()
	{
		stopped = true;
	}

	public List<Runnable> shutdownNow()
	{
		shutdown();
		return new ArrayList<Runnable>();
	}

	public synchronized boolean isShutdown()
	{
		return stopped;
	}

	public boolean isTerminated()
	{
		return isShutdown();
	}

	public boolean awaitTermination(long timeout, TimeUnit unit)
	    throws InterruptedException
	{
		return true;
	}

	private void checkNotStopped()
	{
		if(isShutdown())
			throw new RejectedExecutionException();
	}

	public void execute(Runnable command)
	{
		checkNotStopped();
		command.run();
	}

	public <T> Future<T> submit(Callable<T> task)
	{
		checkNotStopped();

		try {
			return new DummyFuture<T>( task.call(), null );
		} catch ( Exception e ) {
			return new DummyFuture<T>( null, e );
		}
	}

	public <T> Future<T> submit(Runnable task, T result)
	{
		try {
			task.run();
			return new DummyFuture<T>( result, null );
		} catch ( Exception e ) {
			return new DummyFuture<T>( null, e );
		}
	}

	public Future<?> submit(Runnable task)
	{
		return submit( task, null );
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
	    throws InterruptedException
	{
		List<Future<T>> result = new ArrayList<Future<T>>();

		for(Callable<T> task : tasks) {
			result.add( submit( task ) );
		}

		return result;
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
	                              long timeout, TimeUnit unit)
	    throws InterruptedException
	{
		return invokeAll( tasks );
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
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

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
	                long timeout, TimeUnit unit)
	    throws InterruptedException, ExecutionException, TimeoutException
	{
		return invokeAny( tasks );
	}

}
