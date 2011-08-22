package com.foreach.concurrent;

import org.apache.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class SynchronousTaskExecutor implements TaskExecutorService
{
	private static final Logger LOG = Logger.getLogger( SynchronousTaskExecutor.class );

	/**
	 * Execute the task synchronously.
	 * @param task the Task to be executed.
	 */

	public final void executeTask(Task task)
	{
		try {
			task.execute();
		} catch ( Exception e ) {
			LOG.error( "Error in task", e );
		}
	}

	/**
	 * Execute the callable synchronously.
	 * @param callable the callable to be executed.
	 * @return a completed Future, that can not be cancelled.
	 */

	public final <V> Future<V> executeCallable( Callable<V> callable)
	{
		try {
			return new DummyFuture<V>( callable.call() );
		} catch ( Exception e ) {
			LOG.error( "Error in callable", e );
			return null;
		}
	}

}
