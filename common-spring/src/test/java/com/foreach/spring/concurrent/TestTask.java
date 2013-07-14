package com.foreach.spring.concurrent;

public class TestTask implements Runnable
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
		}
	}

}
