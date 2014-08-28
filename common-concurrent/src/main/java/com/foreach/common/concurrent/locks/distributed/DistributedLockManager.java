package com.foreach.common.concurrent.locks.distributed;

import java.util.concurrent.TimeUnit;

/**
 * Instance that is the management entry point for DistributedLock implementations.
 * Takes care of the actual acquiring, releasing and notifying (keep alive) of distributed locks
 * that have been dealt out.
 */
public interface DistributedLockManager
{
	/**
	 * Will try and acquire the lock and will block the thread until this succeeds.
	 * A {@link com.foreach.common.concurrent.locks.distributed.DistributedLockWaitException} will be thrown
	 * in case an exception occurs (like the thread being interrupted while waiting).
	 *
	 * @param lock Lock instance to acquire.
	 */
	void acquire( DistributedLock lock );

	/**
	 * Will try and acquire the lock and will block the thread until this succeeds.
	 * A checked {@link java.lang.InterruptedException} will be thrown in case the Thread is intterupted
	 * while trying to acquire the lock.
	 *
	 * @param lock Lock instance to acquire.
	 * @throws InterruptedException thrown if Thread was interrupted during wait
	 */
	void acquireInterruptibly( DistributedLock lock ) throws InterruptedException;

	/**
	 * Will release the lock.
	 *
	 * @param lock Lock instance to release.
	 */
	void release( DistributedLock lock );

	/**
	 * Will try and acquire the lock a single time and will return immediately after trying.
	 * The return value determines if the acquiring succeeded or failed.
	 *
	 * @param lock Lock instance to acquire.
	 * @return {@code true} if the lock was acquired and {@code false} otherwise
	 */
	boolean tryAcquire( DistributedLock lock );

	/**
	 * Will try and acquire the lock and will keep trying until the specified time has passed.
	 * The return value determines if the acquiring succeeded or failed.
	 *
	 * @param lock Lock instance to acquire.
	 * @param time the maximum time to wait for the lock
	 * @param unit the time unit of the {@code time} argument
	 * @return {@code true} if the lock was acquired and {@code false} if the waiting time elapsed before the lock was acquired
	 */
	boolean tryAcquire( DistributedLock lock, long time, TimeUnit unit );

	/**
	 * Checks if the lock is held by anybody.
	 *
	 * @param lockKey Id of the lock to check.
	 * @return {@code true} if the lock is held by anyone.
	 */
	boolean isLocked( String lockKey );

	/**
	 * Checks if the lock is held by a specific owner.
	 *
	 * @param ownerId Id of the owner.
	 * @param lockKey  Id of the lock to check.
	 * @return {@code true} if the lock is held by that owner
	 */
	boolean isLockedByOwner( String ownerId, String lockKey );

	/**
	 * Verifies that the lock is still active.  This is the most expensive but secure check
	 * that a lock has not been stolen.  This call assumes that the lock is held by the owner,
	 * and in case it is not will report it as stolen and fire the (optional) stolen callback.
	 *
	 * @param ownerId Id of the owner to verify.
	 * @param lockKey  Id of the lock to verify.
	 * @return {@code true} if the lock was held by the owner
	 */
	boolean verifyLockedByOwner( String ownerId, String lockKey );
}
