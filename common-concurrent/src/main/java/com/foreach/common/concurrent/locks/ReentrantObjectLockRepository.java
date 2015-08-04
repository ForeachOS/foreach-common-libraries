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
package com.foreach.common.concurrent.locks;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * <p>
 * Provides a repository that maintains a lock for a key of type T.  The same lock will be returned as long as the key is equal.
 * The instance returned is of type ObjectLock<T> and will support reentrant locking.  This repository uses WeakReferences
 * to ensure that it does not keep existing lock keys indefinitely.
 * to manage itself.
 * </p>
 * <p>Access to the ReentrantObjectLockRepository is synchronized.</p>
 *
 * @param <T> type of the key
 */
public class ReentrantObjectLockRepository<T> implements ObjectLockRepository<T>
{
	private final Map<T, WeakReference<ObjectLock<T>>> locks = new WeakHashMap<>();

	/**
	 * Fetches the lock with the specific key.  Will create an ObjectLock if necessary.
	 *
	 * @param key Key for the ObjectLock instance
	 * @return ObjectLock instance that can be locked
	 */
	@Override
	public synchronized ObjectLock<T> getLock( T key ) {
		WeakReference<ObjectLock<T>> lockReference = locks.get( key );

		ObjectLock<T> lock = lockReference != null ? lockReference.get() : null;

		if ( lock == null ) {
			lock = new ReentrantObjectLock<T>( key );
			locks.put( key, new WeakReference<>( lock ) );
		}

		return lock;
	}

	/**
	 * Fetches the lock with the specific key, and blocks until the lock is acquired
	 * by the current thread as well. The instance returned is {@link java.lang.AutoCloseable}
	 * and can be used in a try-with-resources construct.
	 *
	 * @param key Key for the ObjectLock instance
	 * @return ObjectLock instance that is held by the current thread.
	 */
	@Override
	public CloseableObjectLock<T> lock( T key ) {
		CloseableObjectLock<T> lock = new CloseableObjectLock<T>( getLock( key ) );
		lock.lock();
		return lock;
	}
}
