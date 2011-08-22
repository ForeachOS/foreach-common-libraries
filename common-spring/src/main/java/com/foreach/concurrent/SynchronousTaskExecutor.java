package com.foreach.concurrent;

import org.apache.log4j.Logger;

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
		} catch ( Throwable throwable ) {
			LOG.error( "Error in task", throwable );
		}
	}
}
