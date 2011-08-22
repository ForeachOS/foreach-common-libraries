package com.foreach.concurrent;

import com.foreach.utils.BaseTestService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class TestAsynchronousTaskExecutor extends BaseTestService
{
	@Autowired
	@Qualifier("asynchronousTaskExecutor")
	private TaskExecutorService asyncTaskExecutor;

	@Before
	public void prepareForTest()
	{
	}

	@Test
	public void executionIsAsynchronous()
	{
		long startTime = System.currentTimeMillis();

		asyncTaskExecutor.executeTask( new AbstractTask()
		{
			public void execute()
			{
				try {
					Thread.sleep( 2000 );
				}
				catch ( InterruptedException ie ) {
				}
			}
		} );

		long delta = System.currentTimeMillis() - startTime;

		Assert.assertTrue( delta <200 );
	}
}
