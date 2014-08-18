package com.foreach.common.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * Provides a repository that maintains a lock for a key of type T.The same lock will be returned as long as the key is equal.
 * The instance returned is of type ObjectLock and holds a back reference to the repository, making it possible for the repository
 * to manage itself.
 * </p>
 * <p>
 * A used ObjectLock should always be released using the {@link ObjectLock#release() release()} method, else it will exist in the repository forever.  After release()
 * has been called the same instance should not be used again without fetching from the repository.  If the same ObjectLock is meant
 * to be locked/unlocked multiple times by the same thread, the {@link ObjectLock#unlock() unlock()} method should be used instead.
 * </p>
 * <p>Access to the ObjectLockRepository is synchronized.</p>
 *
 * @param <T> type of the key
 */
public class ObjectLockRepository<T> {
    private final Map<T, ObjectLock<T>> locks = new HashMap<T, ObjectLock<T>>();

    /**
     * Fetches the lock with the specific key.  Will create an ObjectLock if necessary.
     *
     * @param key Key for the ObjectLock instance
     * @return ObjectLock instance that can be locked
     */
    public synchronized ObjectLock<T> getLock( T key ) {
        ObjectLock<T> lock = locks.get( key );

        if( lock == null ) {
            lock = new ObjectLock<T>( this, key );
            locks.put( key, lock );
        }

        lock.register();

        return lock;
    }

    /**
     * Removes the lock from the internal map.
     */
    synchronized void release( ObjectLock<T> lock ) {
        lock.unregister();

        ReentrantLock reentrantLock = lock.getLock();

        if( !reentrantLock.hasQueuedThreads() ) {
            if( lock.destroy() ) {
                locks.remove( lock.getKey() );
            }
        }
    }

    /**
     * @return Number of registered ObjectLock instances.
     */
    public int size() {
        return locks.size();
    }
}
