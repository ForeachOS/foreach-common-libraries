package com.foreach.concurrent;

import com.foreach.utils.BaseTestService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class TestSynchronousTaskExecutor extends BaseTestService
{
	@Autowired
	@Qualifier("synchronousTaskExecutor")
	private TaskExecutorService synchronousTaskExecutor;

	@Before
	public void prepareForTest()
	{
	}

	@Test
	public void executionIsSynchronous()
	{
		long startTime = System.currentTimeMillis();

		synchronousTaskExecutor.executeTask( new AbstractTask()
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

		Assert.assertTrue( delta >= 2000 );
	}

}
