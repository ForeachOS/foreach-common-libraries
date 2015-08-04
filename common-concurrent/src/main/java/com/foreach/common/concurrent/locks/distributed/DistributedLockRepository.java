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

import com.foreach.common.concurrent.locks.ObjectLockRepository;

/**
 * Central repository for creating distributed locks.
 *
 * @author Arne Vandamme
 */
public interface DistributedLockRepository extends ObjectLockRepository<String>
{
	/**
	 * Creates a new DistributedLock with a unique owner id that is safe to use in distributed and
	 * multi-threaded scenarios.  This lock instance does not require manual management of the owner id
	 * or threads trying to acquire/release the lock.
	 * <p/>
	 * The generated owner id is based on a UUID and guaranteed to be unique.
	 *
	 * @param key Unique id of the lock.
	 * @return DistributedLock instance.
	 * @see ThreadBasedDistributedLock
	 */
	DistributedLock getLock( String key );

	/**
	 * Creates a new DistributedLock with a unique owner id that is safe to use in distributed and
	 * multi-threaded scenarios.  This lock instance does not require manual management of the threads
	 * trying to acquire/release the lock within a same vm.
	 * <p/>
	 * For descriptive purposes the owner name passed in will be used as prefix for the owner id.
	 * <p/>
	 * <strong>Note:</strong> in distributed contexts it is imperative (unless explicitly required) that
	 * the ownerName be different in different applications or vms, as collisions can occur with the
	 * generated owner ids otherwise.
	 *
	 * @param ownerName Name of the owner, will be used as part of the unique owner id.
	 * @param key       Unique id of the lock.
	 * @return DistributedLock instance.
	 * @see ThreadBasedDistributedLock
	 */
	DistributedLock getLock( String ownerName, String key );

	/**
	 * Creates a new shared distributed lock with the specified owner id.  This lock instance
	 * will require manual management across separate threads within the same vm.
	 *
	 * @param ownerId Unique id of the owner for this lock.
	 * @param lockKey Unique id of the lock.
	 * @return DistributedLock instance.
	 * @see SharedDistributedLock
	 */
	DistributedLock createSharedLock( String ownerId, String lockKey );
}
