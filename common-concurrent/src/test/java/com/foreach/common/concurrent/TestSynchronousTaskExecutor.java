package com.foreach.common.concurrent;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertTrue;

public class TestSynchronousTaskExecutor
{
	private static final int DELAY = 444;

	private ExecutorService synchronousTaskExecutor = new SynchronousTaskExecutor();

	@Test
	public void taskExecutionIsSynchronous() {
		long startTime = System.currentTimeMillis();

		synchronousTaskExecutor.execute( new TestTask( DELAY ) );

		long delta = System.currentTimeMillis() - startTime;

		assertTrue( delta >= DELAY );
	}

	@Test
	public void callableExecutionIsSynchronous() {
		long startTime = System.currentTimeMillis();

		synchronousTaskExecutor.submit( new TestCallable( DELAY ) );

		long delta = System.currentTimeMillis() - startTime;

		assertTrue( delta >= DELAY );
	}

	private static class TestTask implements Runnable
	{
		private int delay;

		public TestTask( int delay ) {
			this.delay = delay;
		}

		public void run() {
			try {
				Thread.sleep( delay );
			}
			catch ( InterruptedException ie ) {
				ie.printStackTrace();
			}
		}
	}

	private static class TestCallable implements Callable<Integer>
	{
		private int delay;

		public TestCallable( int delay ) {
			this.delay = delay;
		}

		public Integer call() {
			try {
				Thread.sleep( delay );
				return new Integer( delay );
			}
			catch ( InterruptedException ie ) {
				ie.printStackTrace();
				return null;
			}
		}
	}
}
