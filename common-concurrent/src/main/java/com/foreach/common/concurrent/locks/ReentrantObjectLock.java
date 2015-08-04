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
