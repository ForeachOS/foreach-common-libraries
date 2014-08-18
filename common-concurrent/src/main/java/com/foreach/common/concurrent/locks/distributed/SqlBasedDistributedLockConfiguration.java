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
	public static final int MAX_LOCK_KEY_LENGTH = 120;

	/**
	 * Maximum length for a valid owner id.
	 */
	public static final int MAX_OWNER_ID_LENGTH = 120;

	private final String tableName;

	private long retryInterval = DEFAULT_RETRY_INTERVAL;
	private long verifyInterval = DEFAULT_VERIFY_INTERVAL;
	private long maxIdleBeforeSteal = DEFAULT_MAX_IDLE_BEFORE_STEAL;
	private long cleanupInterval = DEFAULT_CLEANUP_INTERVAL;
	private long cleanupAge = DEFAULT_MIN_AGE_BEFORE_DELETE;

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
}
