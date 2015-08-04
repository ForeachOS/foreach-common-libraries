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
