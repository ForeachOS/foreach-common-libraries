package com.foreach.concurrent;

import org.apache.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * AsynchronousTaskExecutor allows for the asynchronous execution of Tasks and Callables.
 * <p/>
 * Upon instantiation, the AsynchronousTaskExecutor will have a default ExecutorService configured,
 * so you only need to call setExecutorService if you want to use an ExecutorService other than the default.
 */

public class AsynchronousTaskExecutor implements TaskExecutorService
{
	private static final Logger LOG = Logger.getLogger( AsynchronousTaskExecutor.class );

	private ExecutorService executorService = new ScheduledThreadPoolExecutor( 2 );

	/**
	 * Set the ExecutorService to be used.
	 *
	 * @param executorService the ExecutorService used to perform Tasks and Callables.
	 *                        <p/>
	 *                        Note that pending Tasks or Callables that were submitted prior to changing the executorService will
	 *                        still be executed in the previous executorService.
	 */
	public final synchronized void setExecutorService( ExecutorService executorService )
	{
		this.executorService = executorService;
	}

	/**
	 * Get the ExecutorService being used.
	 */
	public final synchronized ExecutorService getExecutorService()
	{
		return this.executorService;
	}

	/**
	 * Execute a Task asynchronously.
	 *
	 * @param task the Task to be executed.
	 */
	public final void executeTask( final Task task )
	{
		this.executeCallable( new CallableWrapper( task ) );
	}

	/**
	 * Execute a Callable asynchronously.
	 *
	 * @param callable the Callable to be executed.
	 * @return a Future.
	 */
	public final <V> Future<V> executeCallable( Callable<V> callable )
	{
		try {
			return getExecutorService().submit( callable );
		}
		catch ( Exception e ) {
			LOG.error( "Error in task or callable", e );
			return null;
		}
	}

	// convert a Task to a Callable
	private static class CallableWrapper implements Callable<Object>
	{
		private final Task task;

		public CallableWrapper( Task task )
		{
			this.task = task;
		}

		public Object call()
		{
			task.execute();
			return null;
		}
	}
}
