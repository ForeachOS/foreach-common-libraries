package com.foreach.spring.concurrent;

import com.foreach.spring.util.BaseTestService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.ExecutorService;

public class TestSynchronousTaskExecutor extends BaseTestService
{
	@Autowired
	@Qualifier("synchronousTaskExecutor")
	private ExecutorService synchronousTaskExecutor;

	@Before
	public void prepareForTest()
	{
	}

	@Test
	@Ignore
	public void taskExecutionIsSynchronous()
	{
		long startTime = System.currentTimeMillis();

		synchronousTaskExecutor.execute( new TestTask( 2000 ) );

		long delta = System.currentTimeMillis() - startTime;

		Assert.assertTrue( delta >= 2000 );
	}

	@Test
	@Ignore
	public void callableExecutionIsSynchronous()
	{
		long startTime = System.currentTimeMillis();

		synchronousTaskExecutor.submit( new TestCallable( 2000 ) );

		long delta = System.currentTimeMillis() - startTime;

		Assert.assertTrue( delta >= 2000 );
	}
}
