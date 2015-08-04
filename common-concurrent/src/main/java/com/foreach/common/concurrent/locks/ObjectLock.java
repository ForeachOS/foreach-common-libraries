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

import java.util.concurrent.locks.Lock;

/**
 * @author Arne Vandamme
 */
public interface ObjectLock<T> extends Lock
{
	/**
	 * @return The object that this lock is for.
	 */
	T getKey();

	/**
	 * Queries if this lock is held by any thread. This method is
	 * designed for use in monitoring of the system state,
	 * not for synchronization control.
	 *
	 * @return {@code true} if any thread holds this lock and
	 * {@code false} otherwise
	 */
	boolean isLocked();

	/**
	 * Queries if this lock is held by the current thread.
	 *
	 * @return {@code true} if this thread holds this lock and
	 * {@code false} otherwise
	 */
	boolean isHeldByCurrentThread();
}
