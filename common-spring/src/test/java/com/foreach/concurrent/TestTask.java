package com.foreach.concurrent;


public class TestTask implements Task
{
	private int delay;

	public TestTask( int delay )
	{
		this.delay = delay;
	}

	public void execute()
	{
		try {
			Thread.sleep( delay );
		}
		catch ( InterruptedException ie ) {
		}
	}

}
