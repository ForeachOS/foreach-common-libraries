package com.foreach.concurrent;

import com.foreach.utils.BaseTestService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
	public void taskExecutionIsAsynchronous()
	{
		long startTime = System.currentTimeMillis();

		asyncTaskExecutor.executeTask( new TestTask( 2000 ) );

		long delta = System.currentTimeMillis() - startTime;

		Assert.assertTrue( delta <200 );
	}

	@Test
	public void callableExecutionIsAsynchronous()
	{
		long startTime = System.currentTimeMillis();

		asyncTaskExecutor.executeCallable( new TestCallable( 2000 ) );

		long delta = System.currentTimeMillis() - startTime;

		Assert.assertTrue( delta <200 );
	}

	@Test
	@Ignore // For some reason, tests fails on build server...
	public void canBlockIfNeeded()
	{
		long startTime = System.currentTimeMillis();

		Future<Integer> future = asyncTaskExecutor.executeCallable( new TestCallable( 2000 ) );

		try {
			future.get();
		} catch (Exception e) {}

		long delta = System.currentTimeMillis() - startTime;

		Assert.assertTrue( delta >= 2000 );
	}

	@Test
	public void multipleCallables()
	{
		List<Future<Integer>> pendings = new ArrayList<Future<Integer>>();

		int num = 10;

		for(int i=0;i<num;i++) {
			pendings.add( asyncTaskExecutor.executeCallable( new TestCallable( 2000 + i ) ) );
		}

		int sum = 0;

		for(Future<Integer> pending : pendings)
		try {
			sum += pending.get();
		} catch (Exception e) {}

		Assert.assertEquals( num*2000 + (num*(num-1)/2), sum );
	}


}
