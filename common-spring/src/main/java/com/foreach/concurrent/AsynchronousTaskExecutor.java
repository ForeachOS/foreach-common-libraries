package com.foreach.concurrent;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * AsynchronousTaskExecutor allows for the asynchronous execution of Tasks and Callables.
 * Upon instantiation, the AsynchronousTaskExecutor will create an ExecutorService using a small
 * number of threads, so you only need to call setExecutorService() if you want to use a specific ExecutorService.
 */

public class AsynchronousTaskExecutor implements TaskExecutorService
{
	private ExecutorService executorService = new ScheduledThreadPoolExecutor(2);

	/**
	 * Set the ExecutorService to be used.
	 * @param executorService the ExecutorService used to perform Tasks and Callables.
	 * <p>Note that pending Tasks or Callables that were submitted prior to changeing the executorService will
	 * still be executed in the previous executorService.</p>
	 */

	public synchronized void setExecutorService( ExecutorService executorService )
	{
		this.executorService = executorService;
	}

	/**
	 * Get the ExecutorService being used.
	 */

	public synchronized ExecutorService getExecutorService()
	{
		return this.executorService;
	}

	private static final Logger LOG = Logger.getLogger( AsynchronousTaskExecutor.class );

	/**
	 * Execute a Task asynchronously.
	 * @param task the Task to be executed.
	 */

	@Async
	public final void executeTask( final Task task ) {

		this.executeCallable( new Callable<Object>()
		{
			public Object call() throws Exception
			{
				task.execute();
				return null;
			}
		} );
	}

	/**
	 * Execute a Callable asynchronously.
	 * @param callable the Callable to be executed.
	 * @return a Future.
	 */

	public final <V> Future<V> executeCallable( Callable<V> callable)
	{
		try {
			return getExecutorService().submit(callable);
		} catch ( Throwable throwable ) {
			LOG.error( "Error in task or callable", throwable );
			return null;
		}
	}

}
