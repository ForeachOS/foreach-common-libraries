package com.foreach.common.concurrent.locks.distributed;

import com.foreach.common.concurrent.locks.CloseableObjectLock;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Simple implementation of {@link DistributedLockRepository}.
 *
 * @author Arne Vandamme
 */
public class DistributedLockRepositoryImpl implements DistributedLockRepository
{
	private static final String JVM_ID = UUID.randomUUID().toString();

	private final DistributedLockManager lockManager;
	private final String defaultOwnerName;

	public DistributedLockRepositoryImpl( DistributedLockManager lockManager ) {
		this.lockManager = lockManager;
		this.defaultOwnerName = JVM_ID;
	}

	public DistributedLockRepositoryImpl( DistributedLockManager lockManager, String defaultOwnerName ) {
		this.lockManager = lockManager;
		this.defaultOwnerName = defaultOwnerName;
	}

	@Override
	public DistributedLock getLock( String key ) {
		return getLock( defaultOwnerName, key );
	}

	@Override
	public DistributedLock getLock( String ownerName, String key ) {
		Assert.isTrue( StringUtils.hasText( key ), "key must not be empty" );
		Assert.isTrue( StringUtils.hasText( ownerName ), "ownerName must not be empty" );

		return new ThreadBasedDistributedLock( lockManager, ownerName, key );
	}

	@Override
	public DistributedLock createSharedLock( String ownerId, String key ) {
		Assert.isTrue( StringUtils.hasText( key ), "key must not be empty" );
		Assert.isTrue( StringUtils.hasText( ownerId ), "ownerId must not be empty" );

		return new SharedDistributedLock( lockManager, ownerId, key );
	}

	@Override
	public CloseableObjectLock<String> lock( String key ) {
		CloseableObjectLock<String> lock = new CloseableObjectLock<>( getLock( key ) );
		lock.lock();

		return lock;
	}
}
