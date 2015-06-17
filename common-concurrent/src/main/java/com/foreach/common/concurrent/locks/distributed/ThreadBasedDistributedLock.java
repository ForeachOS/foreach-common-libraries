package com.foreach.common.concurrent.locks.distributed;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * Implementation of {@link DistributedLock} that implements
 * behavior most alike standard locks, in such a way that locking happens on a thread basis.
 * <p/>
 * The owner id of this lock is dynamic, based on the thread that it is operating on.
 * In most scenarios this is probably the implementation you want to use.
 * <p/>
 * For an alternative implementation and use case see the {@link SharedDistributedLock}.
 *
 * @author Arne Vandamme
 * @see SharedDistributedLock
 */
public class ThreadBasedDistributedLock implements DistributedLock
{
	private final String ownerId, key;
	private final DistributedLockManager lockManager;

	private LockStolenCallback stolenCallback;
	private LockUnstableCallback unstableCallback;

	ThreadBasedDistributedLock( DistributedLockManager lockManager, String owner, String key ) {
		this.ownerId = owner;
		this.key = key;
		this.lockManager = lockManager;
	}

	@Override
	public String getOwnerId() {
		Thread currentThread = Thread.currentThread();
		return String.format( "%s[%s@%s])", ownerId, currentThread.getId(), System.identityHashCode( currentThread ) );
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public void lock() {
		lockManager.acquire( this );
	}

	@Override
	public boolean tryLock() {
		return lockManager.tryAcquire( this );
	}

	@Override
	public boolean tryLock( long time, TimeUnit unit ) {
		return lockManager.tryAcquire( this, time, unit );
	}

	@Override
	public boolean isLocked() {
		return lockManager.isLocked( getKey() );
	}

	@Override
	public boolean isHeldByCurrentThread() {
		return lockManager.isLockedByOwner( getOwnerId(), getKey() );
	}

	@Override
	public void unlock() {
		lockManager.release( this );
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		lockManager.acquireInterruptibly( this );
	}

	@Override
	public Condition newCondition() {
		throw new UnsupportedOperationException(
				"newCondition() is not supported on this DistributedLock implementation." );
	}

	@Override
	public LockStolenCallback getStolenCallback() {
		return stolenCallback;
	}

	@Override
	public void setStolenCallback( LockStolenCallback stolenCallback ) {
		this.stolenCallback = stolenCallback;
	}

	@Override
	public LockUnstableCallback getUnstableCallback() {
		return unstableCallback;
	}

	@Override
	public void setUnstableCallback( LockUnstableCallback unstableCallback ) {
		this.unstableCallback = unstableCallback;
	}
}
