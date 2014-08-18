package com.foreach.common.concurrent.locks.distributed;

/**
 * Implementation of {@link DistributedLock} that uses the specified
 * owner id to manage the lock.  Unlike the {@link ThreadBasedDistributedLock}
 * this implementation does not take the actual thread into account when determining the owner id.
 *
 * The uniqueness of the owner id is entirely up to the end user.
 *
 * A SharedDistributedLock can be useful for situations where you want different threads to be able to acquire
 * or release the lock; eg in a master/slave setup.
 *
 * @see ThreadBasedDistributedLock
 */
public class SharedDistributedLock extends ThreadBasedDistributedLock
{
	private final String ownerId;

	SharedDistributedLock( DistributedLockManager lockManager, String ownerId, String lockKey ) {
		super( lockManager, ownerId, lockKey );

		this.ownerId = ownerId;
	}

	@Override
	public String getOwnerId() {
		return ownerId;
	}
}
