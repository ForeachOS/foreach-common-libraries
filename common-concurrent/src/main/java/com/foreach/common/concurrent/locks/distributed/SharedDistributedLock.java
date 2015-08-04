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
