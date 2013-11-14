package com.foreach.spring.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Provides a lock object that is managed by a central repository and identified by a key of type T.
 *
 * @param <T> type of the key associated with this lock
 */
public class ObjectLock<T> {
    private final ReentrantLock lock = new ReentrantLock();

    private final T key;
    private ObjectLockRepository<T> repository;

    private AtomicInteger delivered = new AtomicInteger( 0 );

    ObjectLock( ObjectLockRepository<T> repository, T key ) {
        this.repository = repository;
        this.key = key;
    }

    public T getKey() {
        return key;
    }

    ReentrantLock getLock() {
        return this.lock;
    }

    /**
     * Called from the repository whenever a client registers for this lock.
     */
    void register() {
        delivered.incrementAndGet();
    }

    /**
     * Called from the repository whenever a client unregisters for this lock.
     */
    void unregister() {
        delivered.decrementAndGet();
    }

    /**
     * Called from the repository when the last thread unlocks.
     */
    boolean destroy() {
        if( this.delivered.get() == 0 ) {
            repository = null;
        }

        return repository == null;
    }

    /**
     * Secures the lock.
     */
    public void lock() {
        if( repository == null ) {
            throw new RuntimeException( "ObjectLock has been destroyed - a new instance should be fetched from the repository." );
        }

        lock.lock();
    }

    /**
     * Releases the actual thread lock, but keeps the ObjectLock intact.
     * Only the thread that owns the lock can unlock it.
     */
    public void unlock() {
        if( lock.isHeldByCurrentThread() ) {
            lock.unlock();
        }
    }

    /**
     * Unlocks and releases the ObjectLock - communicating it will not be used again in the same thread.
     * After a call to release() the instance should not longer be used, summarized: the release() call should be
     * the last method call on the ObjectLock instance.
     */
    public void release() {
        unlock();
        repository.release( this );
    }
}
