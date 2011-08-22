package com.foreach.concurrent;

/**
 * <p>A Task is a computional unit without an explicit result.</p>
 *
 * <p>A typical usecase would be some action you would have the option to perform asynchronously,
 * without needing the option to cancel it, or look at the eventual result at a later time.
 * ( In these cases, you should use {@link java.util.concurrent.Future } ).
 * </p>
 */

public interface Task
{
	/**
	 *  Execute the task.
	 */
	void execute();
}
