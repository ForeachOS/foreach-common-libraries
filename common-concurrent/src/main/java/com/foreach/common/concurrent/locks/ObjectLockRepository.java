package com.foreach.common.concurrent.locks;

/**
 * @author Arne Vandamme
 */
public interface ObjectLockRepository<T>
{
	/**
	 * Fetches the lock with the specific key.  Will create an ObjectLock if necessary.
	 *
	 * @param key Key for the ObjectLock instance
	 * @return ObjectLock instance that can be locked
	 */
	ObjectLock<T> getLock( T key );

	/**
	 * Fetches the lock with the specific key, and locks at the same time.
	 * The instance returned is {@link AutoCloseable} and can be used in a
	 * try-with-resources construct.
	 *
	 * @param key Key for the ObjectLock instance
	 * @return ObjectLock instance that is held by the current thread.
	 */
	CloseableObjectLock<T> lock( T key );
}
