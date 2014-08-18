package com.foreach.common.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * SynchronousTaskExecutor is a partial synchronous implementation of ExecutorService.
 * <p/>
 * On methods with timeout, the timeout values are ignored.
 * <p/>
 * Note that executing tasks synchronously may cause scaling issues if there is no upper bound
 * on the number of concurrent threads the executor instance is called from.
 */
public class SynchronousTaskExecutor implements ExecutorService
{
	private Logger logger = LoggerFactory.getLogger( getClass() );

	private volatile boolean stopped = false;

	/**
	 * Set the logger for ths instance
	 */
	protected final void setLogger( Logger log ) {
		this.logger = log;
	}

	/**
	 * Get the logger for this instance
	 */
	protected final Logger getLogger() {
		return logger;
	}

	/**
	 * Prevent the executor from accepting any more tasks.
	 */
	public final synchronized void shutdown() {
		stopped = true;
	}

	/**
	 * Prevent the executor from accepting any more tasks and return an empty List.
	 */
	// Spec says we should interrupt all tasks in progress here...
	public final List<Runnable> shutdownNow() {
		shutdown();
		return new ArrayList<Runnable>();
	}

	/**
	 * Returns true if the executor no longer accepts tasks
	 */
	public final synchronized boolean isShutdown() {
		return stopped;
	}

	/**
	 * Returns true if the executor no longer accepts tasks
	 */
	public final boolean isTerminated() {
		return isShutdown();
	}

	/**
	 * Returns true
	 */
	public final boolean awaitTermination( long timeout, TimeUnit unit ) throws InterruptedException {
		return true;
	}

	private void checkNotStopped() {
		if ( isShutdown() ) {
			throw new RejectedExecutionException();
		}
	}

	/**
	 * If the executor is not shut down, the command will be executed synchronously,
	 * otherwise a RejectedExecutionException is thrown.
	 */
	public final void execute( Runnable command ) {
		checkNotStopped();
		command.run();
	}

	/**
	 * If the executor is not shut down, the task will be executed synchronously
	 * and its result returned,
	 * otherwise a RejectedExecutionException is thrown.
	 */
	public final <T> Future<T> submit( Callable<T> task ) {
		checkNotStopped();

		try {
			return new PreComputedFuture<T>( task.call(), null );
		}
		catch ( Exception e ) {
			return new PreComputedFuture<T>( null, e );
		}
	}

	/**
	 * If the executor is not shut down, the task will be executed synchronously
	 * and the passed result will be wrapped in a future,
	 * otherwise a RejectedExecutionException is thrown.
	 */
	public final <T> Future<T> submit( Runnable task, T result ) {
		try {
			task.run();
			return new PreComputedFuture<T>( result, null );
		}
		catch ( Exception e ) {
			return new PreComputedFuture<T>( null, e );
		}
	}

	/**
	 * If the executor is not shut down, the task will be executed synchronously
	 * and the future with a null result will be returned,
	 * otherwise a RejectedExecutionException is thrown.
	 */

	public final Future<?> submit( Runnable task ) {
		return submit( task, null );
	}

	public final <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks ) throws InterruptedException {
		List<Future<T>> result = new ArrayList<Future<T>>();

		for ( Callable<T> task : tasks ) {
			result.add( submit( task ) );
		}

		return result;
	}

	public final <T> List<Future<T>> invokeAll( Collection<? extends Callable<T>> tasks,
	                                            long timeout,
	                                            TimeUnit unit ) throws InterruptedException {
		return invokeAll( tasks );
	}

	public final <T> T invokeAny( Collection<? extends Callable<T>> tasks ) throws InterruptedException, ExecutionException {
		for ( Callable<T> task : tasks ) {
			try {
				return task.call();
			}
			catch ( Exception e ) {

			}
		}

		throw new ExecutionException( "No task completed successfully", null );
	}

	public final <T> T invokeAny( Collection<? extends Callable<T>> tasks,
	                              long timeout,
	                              TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
		return invokeAny( tasks );
	}
}
