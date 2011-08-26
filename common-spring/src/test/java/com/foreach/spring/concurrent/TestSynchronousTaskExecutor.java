package com.foreach.spring.concurrent;

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
	public void taskExecutionIsSynchronous()
	{
		long startTime = System.currentTimeMillis();

		synchronousTaskExecutor.executeTask( new TestTask( 2000 ) );

		long delta = System.currentTimeMillis() - startTime;

		Assert.assertTrue( delta >= 2000 );
	}

	@Test
	public void callableExecutionIsSynchronous()
	{
		long startTime = System.currentTimeMillis();

		synchronousTaskExecutor.executeCallable( new TestCallable( 2000 ) );

		long delta = System.currentTimeMillis() - startTime;

		Assert.assertTrue( delta >= 2000 );
	}
}
