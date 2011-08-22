package com.foreach.service;

import java.util.Stack;

public interface Task
{
	void execute();

	Stack getInheritedContext();
}
