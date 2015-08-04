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
package com.foreach.common.concurrent.locks.distributed;

import org.springframework.util.Assert;

/**
 * Configuration class for a {@link com.foreach.common.concurrent.locks.distributed.SqlBasedDistributedLockManager}.
 *
 * @author Arne Vandamme
 */
public class SqlBasedDistributedLockConfiguration
{
	/**
	 * Number of milliseconds between tries for acquiring a lock.
	 */
	public static final long DEFAULT_RETRY_INTERVAL = 533;

	/**
	 * Number of milliseconds between monitor runs that will update all actively held locks.
	 */
	public static final long DEFAULT_VERIFY_INTERVAL = 3000;

	/**
	 * Number of milliseconds that a held lock can go without update before another owner can steal it.
	 */
	public static final long DEFAULT_MAX_IDLE_BEFORE_STEAL = 15000;

	/**
	 * Number of milliseconds a lock record should be unlocked before it gets actually deleted from the store.
	 */
	public static final long DEFAULT_MIN_AGE_BEFORE_DELETE = 3600000;

	/**
	 * Number of milliseconds between running the database cleanup.
	 */
	public static final long DEFAULT_CLEANUP_INTERVAL = 900000;

	/**
	 * Maximum length for a valid lock key.
	 */
	public static final int DEFAULT_MAX_LOCK_KEY_LENGTH = 150;

	/**
	 * Maximum length for a valid owner id.
	 */
	public static final int DEFAULT_MAX_OWNER_ID_LENGTH = 150;

	private final String tableName;

	private long retryInterval = DEFAULT_RETRY_INTERVAL;
	private long verifyInterval = DEFAULT_VERIFY_INTERVAL;
	private long maxIdleBeforeSteal = DEFAULT_MAX_IDLE_BEFORE_STEAL;
	private long cleanupInterval = DEFAULT_CLEANUP_INTERVAL;
	private long cleanupAge = DEFAULT_MIN_AGE_BEFORE_DELETE;
	private long maxKeyLength = DEFAULT_MAX_LOCK_KEY_LENGTH;
	private long maxOwnerIdLength = DEFAULT_MAX_OWNER_ID_LENGTH;

	public SqlBasedDistributedLockConfiguration( String tableName ) {
		Assert.notNull( tableName );
		Assert.hasText( tableName );

		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public long getRetryInterval() {
		return retryInterval;
	}

	public void setRetryInterval( long retryInterval ) {
		this.retryInterval = retryInterval;
	}

	public long getVerifyInterval() {
		return verifyInterval;
	}

	public void setVerifyInterval( long verifyInterval ) {
		this.verifyInterval = verifyInterval;
	}

	public long getMaxIdleBeforeSteal() {
		return maxIdleBeforeSteal;
	}

	public void setMaxIdleBeforeSteal( long maxIdleBeforeSteal ) {
		this.maxIdleBeforeSteal = maxIdleBeforeSteal;
	}

	public long getCleanupInterval() {
		return cleanupInterval;
	}

	public void setCleanupInterval( long cleanupInterval ) {
		this.cleanupInterval = cleanupInterval;
	}

	public long getCleanupAge() {
		return cleanupAge;
	}

	public void setCleanupAge( long cleanupAge ) {
		this.cleanupAge = cleanupAge;
	}

	public long getMaxKeyLength() {
		return maxKeyLength;
	}

	public void setMaxKeyLength( long maxKeyLength ) {
		this.maxKeyLength = maxKeyLength;
	}

	public long getMaxOwnerIdLength() {
		return maxOwnerIdLength;
	}

	public void setMaxOwnerIdLength( long maxOwnerIdLength ) {
		this.maxOwnerIdLength = maxOwnerIdLength;
	}
}
