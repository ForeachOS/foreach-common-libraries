package com.foreach.spring.concurrent;

import java.util.concurrent.Callable;

public class TestCallable implements Callable<Integer>
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
			return null;
		}
	}

	;

}
