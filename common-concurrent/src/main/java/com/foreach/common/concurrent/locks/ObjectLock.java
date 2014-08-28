package com.foreach.common.concurrent.locks;

import java.util.concurrent.locks.Lock;

/**
 * @author Arne Vandamme
 */
public interface ObjectLock<T> extends Lock
{
	/**
	 * @return The object that this lock is for.
	 */
	T getKey();

	/**
	 * Queries if this lock is held by any thread. This method is
	 * designed for use in monitoring of the system state,
	 * not for synchronization control.
	 *
	 * @return {@code true} if any thread holds this lock and
	 * {@code false} otherwise
	 */
	boolean isLocked();

	/**
	 * Queries if this lock is held by the current thread.
	 *
	 * @return {@code true} if this thread holds this lock and
	 * {@code false} otherwise
	 */
	boolean isHeldByCurrentThread();
}
