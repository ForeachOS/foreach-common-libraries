/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.common.concurrent.locks.distributed;

import com.foreach.common.concurrent.locks.CloseableObjectLock;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Simple implementation of {@link DistributedLockRepository}.
 *
 * The default owner name is usually associated with the current VM, this ensures owner unicity
 * with the default {@link com.foreach.common.concurrent.locks.distributed.ThreadBasedDistributedLock}.
 * When manually specifying the a default owner name (eg for descriptive purposes), care should be taken
 * that is guaranteed to be unique across multiple applications (or within the scope of entities that should
 * behave as separate applications).  In case of different physical locations, the unique server hostname or ip
 * address might be ok.
 *
 * @author Arne Vandamme
 * @see com.foreach.common.concurrent.locks.distributed.SqlBasedDistributedLockManager
 * @see com.foreach.common.concurrent.locks.distributed.SqlBasedDistributedLockConfiguration
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
