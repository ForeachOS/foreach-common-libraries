package com.foreach.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * TaskExecutorService allows you to execute Tasks and Callables.
 * <p/>
 * Implementations handle the details of when, in what order,
 * and in how many concurrent threads these Tasks and Callables are executed.
 */
public interface TaskExecutorService
{
	/**
	 * Add the Task to the workload of the TaskExecutorService.
	 */
	void executeTask( Task task );

	/**
	 * Add the Callable to the workload of the TaskExecutorService.
	 */
	<V> Future<V> executeCallable( Callable<V> callable );
}
