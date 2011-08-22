package com.foreach.concurrent;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.springframework.scheduling.annotation.Async;

import java.util.Stack;

/**
 * <p>AsynchronousTaskExecutor executes tasks asynchronously, but please note the actual asynchronous functionality is performed
 * in the spring proxy for this object.</p>
 * <p>To function correctly, AsynchronousTaskExecutor must be instantiated by Spring, you must add</p>
 *
 * <pre>
 * &lt;task:annotation-driven/&gt;
 * </pre>
 * <p>to your Spring configuration, and you must make sure the caller calls methods on the Spring proxy.
 * If the caller is also managed by Spring this happens automatically when you autowire the service
 * or otherwise configure it with Spring. If you want to use the AsynchronousTaskExecutor
 * from an Object that is not managed by Spring, you must get hold of the
 * Spring ApplicationContext and use getBean().</p>
 */

public class AsynchronousTaskExecutor implements TaskExecutorService
{

	private static final Logger LOG = Logger.getLogger( AsynchronousTaskExecutor.class );

	/**
	 * Execute the task asynchronously.
	 * @param task the Task to be executed.
	 * <p>Note that when entering this routine control is already transferred to a Spring-managed thread.</p>
	 */


	@Async
	public final void executeTask( Task task ) {

		Stack originalStack = NDC.cloneStack();

		try {
			NDC.inherit( task.getInheritedContext() );
			task.execute();
		} catch ( Throwable throwable ) {
			LOG.error( "Error in asynchronous task", throwable );
		} finally {
			NDC.inherit( originalStack );
		}
	}
}
