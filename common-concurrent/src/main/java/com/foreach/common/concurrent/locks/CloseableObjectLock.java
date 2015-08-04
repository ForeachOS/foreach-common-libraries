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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * {@link java.lang.AutoCloseable} wrapper for an {@link com.foreach.common.concurrent.locks.ObjectLock},
 * the close method is the equivalent of unlocking.
 *
 * @author Arne Vandamme
 */
public class CloseableObjectLock<T> implements ObjectLock<T>, AutoCloseable
{
	private final ObjectLock<T> lock;

	public CloseableObjectLock( ObjectLock<T> lock ) {
		this.lock = lock;
	}

	/**
	 * Unlocks on close.
	 */
	@Override
	public void close() {
		if ( lock.isHeldByCurrentThread() ) {
			lock.unlock();
		}
	}

	@Override
	public T getKey() {
		return lock.getKey();
	}

	@Override
	public boolean isLocked() {
		return lock.isLocked();
	}

	@Override
	public boolean isHeldByCurrentThread() {
		return lock.isHeldByCurrentThread();
	}

	@Override
	public void lock() {
		lock.lock();
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		lock.lockInterruptibly();
	}

	@Override
	public boolean tryLock() {
		return lock.tryLock();
	}

	@Override
	public boolean tryLock( long time, TimeUnit unit ) throws InterruptedException {
		return lock.tryLock( time, unit );
	}

	@Override
	public void unlock() {
		lock.unlock();
	}

	@Override
	public Condition newCondition() {
		return lock.newCondition();
	}
}
