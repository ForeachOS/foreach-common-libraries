package com.foreach.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * <p>TaskExecutorService allows you to execute Tasks and Callables.</p>
 * <p>Implementations will handle the details of when, in what order,
 * and in how many concurrent threads these Tasks and Callables are executed.</p>
 */

public interface TaskExecutorService
{
	/**
	 *  <p>Add the Task to the workload of the TaskExecutorService.</p>
	 */
	void executeTask( Task task );

	/**
	 *  <p>Add the Callable to the workload of the TaskExecutorService.</p>
	 */
	<V> Future<V> executeCallable( Callable<V> callable);
}
