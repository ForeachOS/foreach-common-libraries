package com.foreach.common.concurrent.locks.distributed;

import com.foreach.common.concurrent.locks.ObjectLock;

import java.util.concurrent.TimeUnit;

/**
 * Interface for custom locks that can be obtained through a
 * {@link com.foreach.common.concurrent.locks.distributed.DistributedLockRepository}.
 *
 * @author Arne Vandamme
 */
public interface DistributedLock extends ObjectLock<String>
{
	/**
	 * @return Id of the owner wanting this lock.
	 */
	String getOwnerId();

	/**
	 * @return Key of the lock that the owner is trying to obtain.
	 */
	String getKey();

	/**
	 * Will try to obtain the lock and wait indefinitely to do so.
	 * In case of an interrupt, a {@link DistributedLockWaitException}
	 * will be thrown.
	 */
	void lock();

	/**
	 * Will try to obtain the lock a single time and return immediately.
	 *
	 * @return {@code true} if the lock was acquired and {@code false} otherwise
	 */
	boolean tryLock();

	/**
	 * Will try to obtain the lock and wait for the specified amount of time to do so.
	 * In case of an interrupt, a {@link DistributedLockWaitException}
	 * will be thrown.
	 *
	 * @param time the maximum time to wait for the lock
	 * @param unit the time unit of the {@code time} argument
	 * @return {@code true} if the lock was acquired and {@code false} if the waiting time elapsed before the lock was acquired
	 */
	boolean tryLock( long time, TimeUnit unit );

	/**
	 * Queries if this lock is held by anyone.
	 * <p/>
	 * <strong>Note:</strong> in a distributed context, calls to check if a lock is taken
	 * can be relatively expensive and almost as expensive as trying to acquire the lock.
	 *
	 * @return true if the lock is held by anyone
	 */
	boolean isLocked();

	/**
	 * Queries if this lock is held by the current thread, or in fact by the owner attached
	 * to the lock which might be implementation dependendant.  Some implementations might
	 * not take the actual current thread into account.
	 * <p/>
	 * <strong>Note:</strong> in a distributed context, calls to check if a lock is taken
	 * can be relatively expensive and almost as expensive as trying to acquire the lock.
	 *
	 * @return true if the lock is held by the owner calling the method
	 * @see ThreadBasedDistributedLock
	 * @see SharedDistributedLock
	 */
	boolean isHeldByCurrentThread();

	/**
	 * Will release the lock.
	 */
	void unlock();

	/**
	 * Set the callback to be executed in case this lock gets stolen.
	 *
	 * @param callback Callback instance for this lock
	 * @see LockStolenCallback
	 */
	void setStolenCallback( LockStolenCallback callback );

	/**
	 * @return The callback attached to this lock.
	 */
	LockStolenCallback getStolenCallback();

	/**
	 * A simple callback interface that will be executed when a DistributedLock is reported stolen.
	 */
	interface LockStolenCallback
	{
		/**
		 * The callback provides the original DistributedLock instance that could possibly be
		 * used to retake the lock (implementation dependent).
		 *
		 * @param lockKey Key of the lock that has been stolen.
		 * @param ownerId Id of the owner the lock has been stolen from.
		 * @param lock Instance of the lock that has been stolen.
		 */
		void stolen( String lockKey, String ownerId, DistributedLock lock );
	}
}
