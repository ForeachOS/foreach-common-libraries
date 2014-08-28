package com.foreach.common.concurrent.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Provides a lock object that is managed by a central repository and identified by a key of type T.
 *
 * @param <T> type of the key associated with this lock
 */
public class ReentrantObjectLock<T> implements ObjectLock<T>
{
	private final ReentrantLock lock = new ReentrantLock();

	private final T key;

	ReentrantObjectLock( T key ) {
		this.key = key;
	}

	public T getKey() {
		return key;
	}

	public boolean isLocked() {
		return lock.isLocked();
	}

	public boolean isHeldByCurrentThread() {
		return lock.isHeldByCurrentThread();
	}

	public void lock() {
		lock.lock();
	}

	public void lockInterruptibly() throws InterruptedException {
		lock.lockInterruptibly();
	}

	public boolean tryLock() {
		return lock.tryLock();
	}

	public boolean tryLock( long time, TimeUnit unit ) throws InterruptedException {
		return lock.tryLock( time, unit );
	}

	public Condition newCondition() {
		return lock.newCondition();
	}

	public void unlock() {
		lock.unlock();
	}
}
