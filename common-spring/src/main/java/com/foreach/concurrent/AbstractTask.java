package com.foreach.concurrent;

import org.apache.log4j.NDC;

import java.util.Stack;

public abstract class AbstractTask implements Task
{
	private Stack inheritedContext;

	public AbstractTask()
	{
		this.inheritedContext = NDC.cloneStack();
	}

	public final Stack getInheritedContext()
	{
		return inheritedContext;
	}

	/*
	Call this routine to execute the task.
	*/

	public abstract void execute();

}
