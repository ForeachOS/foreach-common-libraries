package com.foreach.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface TaskExecutorService
{
	void executeTask( Task task );

	<V> Future<V> executeCallable( Callable<V> callable);
}
