package com.foreach.concurrent;

import java.util.Stack;

public interface Task
{
	void execute();

	Stack getInheritedContext();
}
