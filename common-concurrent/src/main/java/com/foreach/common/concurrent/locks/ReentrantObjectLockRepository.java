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
