package com.foreach.common.concurrent.locks;

import java.util.*;
import java.util.concurrent.*;

/**
 * Helper class for synchronization testing of threads with an ObjectLockRepository.
 *
 * @author Arne Vandamme
 */
public class ExecutorBatch implements Callable<ExecutorBatch.Status>
{
	private final ObjectLockRepository<String> lockRepository;
	private final Map<String, Integer> globalResults;

	private final int threadDuration;
	private final int locksPerBatch;
	private final int executorsPerLock;

	public ExecutorBatch( ObjectLockRepository<String> lockRepository,
	                      Map<String, Integer> globalResults,
	                      int threadDuration,
	                      int locksPerBatch,
	                      int executorsPerLock ) {
		this.lockRepository = lockRepository;
		this.globalResults = globalResults;
		this.threadDuration = threadDuration;
		this.locksPerBatch = locksPerBatch;
		this.executorsPerLock = executorsPerLock;
	}

	@Override
	public Status call() throws Exception {
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool( 50 );

		List<Executor> executors = new ArrayList<>( locksPerBatch * executorsPerLock );

		for ( int i = 0; i < locksPerBatch; i++ ) {
			ObjectLock<String> lock = lockRepository.getLock( "batch-lock-" + i );

			for ( int j = 0; j < executorsPerLock; j++ ) {
				executors.add( new Executor( lock ) );
			}
		}

		for ( Executor executor : executors ) {
			fixedThreadPool.submit( executor );
		}

		fixedThreadPool.shutdown();
		fixedThreadPool.awaitTermination( ( locksPerBatch * executorsPerLock * threadDuration ) + 60000,
		                                  TimeUnit.MILLISECONDS );

		Status status = new Status();

		for ( Executor executor : executors ) {
			if ( executor.isFinished() ) {
				status.succeeded++;
				status.duration += executor.getDuration();
			}
			else {
				status.failed++;
			}
		}

		return status;
	}

	public static class Status
	{
		private volatile int succeeded, failed;
		private long duration;

		public int getSucceeded() {
			return succeeded;
		}

		public int getFailed() {
			return failed;
		}

		public long getAverageDuration() {
			return duration / succeeded;
		}
	}

	class Executor implements Runnable
	{
		private final ObjectLock<String> lock;

		private boolean failed;
		private boolean finished;
		private long duration = -1;

		public Executor( ObjectLock<String> lock ) {
			this.lock = lock;
		}

		public boolean isFailed() {
			return failed;
		}

		public boolean isFinished() {
			return finished;
		}

		public long getDuration() {
			return duration;
		}

		@Override
		public void run() {
			try {
				long start = System.currentTimeMillis();

				lock.lock();

				if ( threadDuration > 0 ) {
					Thread.sleep( threadDuration );
				}

				Integer currentCount = globalResults.get( lock.getKey() );
				if ( currentCount == null ) {
					globalResults.put( lock.getKey(), 1 );
				}
				else {
					globalResults.put( lock.getKey(), currentCount + 1 );
				}

				finished = true;

				duration = System.currentTimeMillis() - start;
			}
			catch ( Exception ie ) {
				failed = true;
				ie.printStackTrace();
			}
			finally {
				lock.unlock();
			}
		}
	}

	public static Collection<Status> execute( Collection<ObjectLockRepository<String>> repositories,
	                                          Map<String, Integer> globalResults,
	                                          int threadDuration,
	                                          int locksPerBatch,
	                                          int executorsPerLock
	) throws InterruptedException, ExecutionException {
		ExecutorService batchExecutorService = Executors.newFixedThreadPool( repositories.size() );

		Set<Future<Status>> results = new HashSet<>();

		for ( ObjectLockRepository<String> repository : repositories ) {
			ExecutorBatch batch = new ExecutorBatch( repository, globalResults, threadDuration, locksPerBatch,
			                                         executorsPerLock );
			results.add( batchExecutorService.submit( batch ) );
		}

		batchExecutorService.shutdown();
		batchExecutorService.awaitTermination( ( locksPerBatch * executorsPerLock * threadDuration ) + 90000,
		                                       TimeUnit.MINUTES );

		Set<Status> computedResults = new HashSet<>();
		for ( Future<Status> result : results ) {
			computedResults.add( result.get() );
		}

		return computedResults;
	}
}
