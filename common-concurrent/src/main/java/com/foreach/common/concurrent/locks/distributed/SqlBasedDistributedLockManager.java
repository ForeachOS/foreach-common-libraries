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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Implementation of a {@link DistributedLockManager} that uses a relational dbms
 * as backend for synchronizing the lock access.  This implementation requires a single
 * database table to be present and has several parameters that can be customized (eg. the retry interval when
 * waiting to obtain a lock).  Configuration of all parameters is done through the
 * {@link com.foreach.common.concurrent.locks.distributed.SqlBasedDistributedLockConfiguration}.
 * </p>
 * <p>
 * The database table should have the following structure:
 * <ul>
 * <li>lock_id: string, represents the lock key</li>
 * <li>owner_id: string, represents the owner holding the lock (null if none)</li>
 * <li>created: long, has the timestamp the lock was acquired</li>
 * <li>updated: long, has the timestamp the lock was last updated (keepalive monitor)</li>
 * <li>holds: integer, number of holds on the lock by the owner</li>
 * </ul>
 * Example liquibase script for creating the table:
 * <pre>
 *         <createTable tableName="distributed_locks">
 * 	        <column name="lock_id" type="java.sql.Types.VARCHAR(150)">
 * 		        <constraints nullable="false" primaryKey="true"/>
 * 	        </column>
 * 	        <column name="owner_id" type="java.sql.Types.VARCHAR(150)">
 * 		        <constraints nullable="true"/>
 * 	        </column>
 * 	        <column name="created" type="java.sql.Types.BIGINT">
 * 		        <constraints nullable="false"/>
 * 	        </column>
 * 	        <column name="updated" type="java.sql.Types.BIGINT">
 * 		        <constraints nullable="false"/>
 * 	        </column>
 * 	        <column name="holds" type="java.sql.Types.INTEGER" defaultValueNumeric="0">
 * 		        <constraints nullable="false"/>
 * 	        </column>
 *         </createTable>
 * </pre>
 * </p>
 * <p>
 * <strong>Important notes:</strong>
 * <ul>
 * <li>It is absolutely critical that all application servers using the same distributed locks are time synchronized.
 * A time drift that is larger than the configured verify interval can already cause problems and a drift larger than the
 * maximum idle time before lock steals will render the entire lock repository useless.  If time synchronization is not
 * possible, the stealing of locks should be disabled (by setting an insanely high max idle time).  This means in case of
 * application crash a manual release should be done of all unreleased locks.
 * </li>
 * <li>This DistributedLock implementation has no concept of fairness.  In environments with high
 * contention, it is possible lock starvation occurs.</li>
 * <li>The DistributedLocks are reentrant: the same owner can enter the lock multiple times and have
 * multiple holds on the same lock. For every lock() there must be an unlock() call to release the lock again!
 * A lock will only be released once all holds have been released.</li>
 * <li>The maximum length of the lock key and owner id is determined by the database table and should be set
 * correctly in the configuration.  For this reason verification of lock key and owner length is not done
 * by the lock implementation itself but by the manager when trying to acquire the lock.  Earlier assertions
 * on valid key and owner id should be done by the application. Also note that certain implementations like
 * {@link com.foreach.common.concurrent.locks.distributed.ThreadBasedDistributedLock} generate the actual owner
 * id based on the running thread, meaning the exact length is known late.</li>
 * <li>Once the manager has been closed {@link #close()}, it is no longer usable.</li>
 * </ul>
 * </p>
 * <p>
 * Includes the monitor implementation that notifies the central lock repository on
 * which locks are still being used, as well as the cleanup thread that deletes old
 * unused locks from the database.</p>
 * <p>This distributed lock manager supports stealing of locks and the concept of an unstable lock that can no
 * longer be verified against the backing database.  Usually a lock would only go unstable if database exceptions
 * occur.  See {@link DistributedLock.LockStolenCallback} and {@link DistributedLock.LockUnstableCallback} for
 * more information.  Callbacks can be set on the lock instance level but defaults can be configured on the manager.
 * </p>
 *
 * @see com.foreach.common.concurrent.locks.distributed.DistributedLock
 * @see com.foreach.common.concurrent.locks.distributed.ThreadBasedDistributedLock
 * @see com.foreach.common.concurrent.locks.distributed.SharedDistributedLock
 * @see com.foreach.common.concurrent.locks.distributed.SqlBasedDistributedLockConfiguration
 * @see com.foreach.common.concurrent.locks.distributed.DistributedLockRepository
 */
public class SqlBasedDistributedLockManager implements DistributedLockManager
{
	private static final Logger LOG = LoggerFactory.getLogger( SqlBasedDistributedLockManager.class );

	private static final String SQL_TAKE_LOCK = "UPDATE %s " +
			"SET owner_id = ?, created = ?, updated = ?, holds = holds + 1 " +
			"WHERE lock_id = ? AND (owner_id IS NULL OR owner_id = ?)";
	private static final String SQL_STEAL_LOCK = "UPDATE %s " +
			"SET owner_id = ?, created = ?, updated = ?, holds = 1 " +
			"WHERE lock_id = ? AND (owner_id IS NULL OR (owner_id = ? AND updated = ?))";

	private static final String SQL_SELECT_LOCK = "SELECT lock_id, owner_id, created, updated, holds " +
			"FROM %s " +
			"WHERE lock_id = ?";
	private static final String SQL_INSERT_LOCK = "INSERT INTO %s (lock_id, owner_id, created, updated, holds) " +
			"VALUES (?,?,?,?,1)";
	private static final String SQL_RELEASE_LOCK = "UPDATE %s " +
			"SET owner_id = NULL, holds = 0 " +
			"WHERE lock_id = ? AND owner_id = ? AND holds = 1";
	private static final String SQL_DECREASE_HOLD = "UPDATE %s " +
			"SET holds = holds - 1 " +
			"WHERE lock_id = ? AND owner_id = ? AND holds > 1";
	private static final String SQL_VERIFY_LOCK = "UPDATE %s " +
			"SET updated = ? " +
			"WHERE lock_id = ? AND owner_id = ?";
	private static final String SQL_CLEANUP = "DELETE FROM %s WHERE owner_id IS NULL AND updated < ?";

	private final String sqlTakeLock, sqlStealLock, sqlSelectLock, sqlInsertLock, sqlReleaseLock, sqlDecreaseHold,
			sqlVerifyLock,
			sqlCleanup;

	private final ScheduledExecutorService monitorThread = Executors.newSingleThreadScheduledExecutor();

	private final SqlBasedDistributedLockConfiguration configuration;
	private final JdbcOperations jdbcTemplate;
	private final SqlBasedDistributedLockMonitor lockMonitor;

	private boolean destroyed = false;

	private DistributedLock.LockStolenCallback defaultLockStolenCallback;
	private DistributedLock.LockUnstableCallback defaultLockUnstableCallback;

	public SqlBasedDistributedLockManager( DataSource dataSource, SqlBasedDistributedLockConfiguration configuration ) {
		this( new JdbcTemplate( dataSource ), configuration );
	}

	public SqlBasedDistributedLockManager( JdbcOperations jdbcTemplate,
	                                       SqlBasedDistributedLockConfiguration configuration ) {
		this.configuration = configuration;

		sqlTakeLock = sql( SQL_TAKE_LOCK );
		sqlStealLock = sql( SQL_STEAL_LOCK );
		sqlSelectLock = sql( SQL_SELECT_LOCK );
		sqlInsertLock = sql( SQL_INSERT_LOCK );
		sqlReleaseLock = sql( SQL_RELEASE_LOCK );
		sqlDecreaseHold = sql( SQL_DECREASE_HOLD );
		sqlVerifyLock = sql( SQL_VERIFY_LOCK );
		sqlCleanup = sql( SQL_CLEANUP );

		this.jdbcTemplate = jdbcTemplate;
		lockMonitor = new SqlBasedDistributedLockMonitor( this,
		                                                  configuration.getVerifyInterval() * 2,
		                                                  configuration.getMaxIdleBeforeSteal() );

		//NOTE: Scheduled tasks should NEVER throw exceptions!  The pool would live on, but the task would not...
		monitorThread.scheduleWithFixedDelay( lockMonitor, configuration.getVerifyInterval(),
		                                      configuration.getVerifyInterval(), TimeUnit.MILLISECONDS );
		monitorThread.scheduleWithFixedDelay( new CleanupMonitor(), 0, configuration.getCleanupInterval(),
		                                      TimeUnit.MILLISECONDS );
	}

	private String sql( String template ) {
		return String.format( template, configuration.getTableName() );
	}

	class CleanupMonitor implements Runnable
	{
		@Override
		public void run() {
			try {
				long cleanupStart = System.currentTimeMillis();
				long staleRecordsTimestamp = cleanupStart - configuration.getCleanupAge();
				int recordsDeleted = jdbcTemplate.update( sqlCleanup, staleRecordsTimestamp );

				LOG.info(
						"Deleted {} locks that have been unused for {} ms - cleanup time was {} ms, next run in {} ms",
						recordsDeleted,
						configuration.getCleanupAge(), System.currentTimeMillis() - cleanupStart,
						configuration.getCleanupInterval() );
			}
			catch ( Exception e ) {
				LOG.error( "Exception trying to cleanup unused locks", e );
			}
		}
	}

	/**
	 * @return Callback instance that will be executed if no specific instance configured on a lock.
	 */
	public DistributedLock.LockStolenCallback getDefaultLockStolenCallback() {
		return defaultLockStolenCallback;
	}

	/**
	 * Set the default callback instance to be executed if a lock is stolen but no callback instance
	 * has been configured directly on the {@link DistributedLock}.
	 *
	 * @param defaultLockStolenCallback instance
	 */
	public void setDefaultLockStolenCallback( DistributedLock.LockStolenCallback defaultLockStolenCallback ) {
		this.defaultLockStolenCallback = defaultLockStolenCallback;
	}

	/**
	 * @return Callback instance that will be executed if no specific instance configured on a lock.
	 */
	public DistributedLock.LockUnstableCallback getDefaultLockUnstableCallback() {
		return defaultLockUnstableCallback;
	}

	/**
	 * Set the default callback instance to be executed if a lock goes unstable but no callback instance
	 * has been configured directly on the {@link DistributedLock}.
	 *
	 * @param defaultLockUnstableCallback instance
	 */
	public void setDefaultLockUnstableCallback( DistributedLock.LockUnstableCallback defaultLockUnstableCallback ) {
		this.defaultLockUnstableCallback = defaultLockUnstableCallback;
	}

	public void close() {
		LOG.trace( "Destruction of the distributed lock manager requested" );

		try {
			Map<SqlBasedDistributedLockMonitor.ActiveLock, DistributedLock> activeLocks = lockMonitor.getActiveLocks();

			LOG.info( "Destroying distributed lock manager - releasing {} held locks", activeLocks.size() );

			for ( SqlBasedDistributedLockMonitor.ActiveLock activeLock : activeLocks.keySet() ) {
				release( activeLock.getOwnerId(), activeLock.getLockId() );
			}

			monitorThread.shutdown();

			try {
				monitorThread.awaitTermination( configuration.getVerifyInterval() * 2, TimeUnit.MILLISECONDS );
			}
			catch ( InterruptedException ie ) {
				LOG.warn( "Failed to wait for clean shutdown of lock monitor" );
			}
		}
		finally {
			destroyed = true;
		}
	}

	@Override
	public void acquire( DistributedLock lock ) {
		try {
			acquireInterruptibly( lock );
		}
		catch ( InterruptedException ie ) {
			throw new DistributedLockWaitException( ie );
		}
	}

	@Override
	public void acquireInterruptibly( DistributedLock lock ) throws InterruptedException {
		checkDestroyed();

		boolean acquired = tryAcquire( lock );

		while ( !acquired ) {
			Thread.sleep( configuration.getRetryInterval() );
			acquired = tryAcquire( lock );
		}
	}

	@Override
	public boolean tryAcquire( DistributedLock lock, long time, TimeUnit unit ) {
		checkDestroyed();

		boolean acquired = tryAcquire( lock );

		long delay = configuration.getRetryInterval();
		long timeRemaining = unit.toMillis( time );

		try {
			while ( !acquired && timeRemaining > 0 ) {
				if ( timeRemaining < delay ) {
					delay = timeRemaining;
				}

				Thread.sleep( delay );
				acquired = tryAcquire( lock );

				timeRemaining -= delay;
			}
		}
		catch ( InterruptedException ie ) {
			throw new DistributedLockWaitException( ie );
		}

		return acquired;
	}

	@Override
	public boolean tryAcquire( DistributedLock lock ) {
		checkDestroyed();

		String lockId = lock.getKey();
		String ownerId = lock.getOwnerId();

		verify( lockId, ownerId );

		try {
			return tryAcquire( lockId, ownerId, lock );
		}
		catch ( DistributedLockException dle ) {
			throw dle;
		}
		catch ( Exception e ) {
			throw new DistributedLockException( "Exception when trying to acquire lock " + lockId, e );
		}
	}

	private void verify( String lockId, String ownerId ) {
		Assert.hasText( lockId, "lock key must not be empty" );
		Assert.hasText( ownerId, "owner id must not be empty" );
		Assert.isTrue( lockId.length() <= configuration.getMaxKeyLength(),
		               "lock key cannot be longer than " + configuration.getMaxKeyLength() + " characters" );
		Assert.isTrue( ownerId.length() <= configuration.getMaxOwnerIdLength(),
		               "owner id cannot be longer than " + configuration.getMaxOwnerIdLength() + " characters" );
	}

	private boolean tryAcquire( String lockId, String ownerId, DistributedLock lock ) {
		boolean acquired = false;

		LOG.trace( "Owner {} is trying to acquire lock {}", ownerId, lockId );

		long timestamp = System.currentTimeMillis();
		try {
			int updated;

			try {
				updated = jdbcTemplate.update( sqlTakeLock, ownerId, timestamp, timestamp, lockId, ownerId );
			}
			catch ( DeadlockLoserDataAccessException dle ) {
				LOG.trace( "Deadlock loser for lock  {} - retrying once immediately", lockId );
				updated = jdbcTemplate.update( sqlTakeLock, ownerId, timestamp, timestamp, lockId, ownerId );
			}

			if ( updated > 1 ) {
				throw new DistributedLockException(
						"DistributedLockRepository table corrupt, more than one lock with id " + lockId );
			}

			if ( updated == 1 ) {
				LOG.trace( "Owner {} directly acquired lock {}", ownerId, lockId );
				acquired = true;
			}
			else {
				LockInfo lockInfo = getLockInfo( lockId );

				if ( lockInfo != null ) {
					if ( ownerId.equals( lockInfo.getOwnerId() ) ) {
						acquired = true;
					}
					else {
						timestamp = System.currentTimeMillis();
						long lastUpdateAge = timestamp - lockInfo.getUpdated();
						if ( lastUpdateAge > configuration.getMaxIdleBeforeSteal() ) {
							LOG.trace( "Lock {} was last updated {} ms ago - attempting to steal the lock",
							           lockId, lastUpdateAge );
							updated = jdbcTemplate.update( sqlStealLock, ownerId, timestamp, timestamp, lockId,
							                               lockInfo.getOwnerId(), lockInfo.getUpdated() );

							acquired = updated == 1;
						}
						else if ( LOG.isTraceEnabled() ) {
							long duration = System.currentTimeMillis() - lockInfo.getCreated();
							LOG.trace( "Lock {} is held by {} since {} ms", lockId, lockInfo.getOwnerId(),
							           duration );
						}
					}
				}
				else {
					LOG.trace( "Lock {} currently does not exist, creating", lockId );

					int created;

					try {
						timestamp = System.currentTimeMillis();
						created = jdbcTemplate.update( sqlInsertLock, lockId, ownerId, timestamp, timestamp );
					}
					catch ( DataAccessException dae ) {
						created = 0;
					}

					if ( created != 1 ) {
						LOG.trace( "Failed to create lock record {} - was possibly created in the meantime",
						           lockId );
					}
					else {
						LOG.trace( "Lock {} created by {}", lockId, ownerId );
						acquired = true;
					}
				}
			}

			if ( acquired ) {
				lockMonitor.addLock( ownerId, lock );
			}
			else {
				// Cleanup any stale record already, we're sure we no longer have the lock
				lockMonitor.removeLock( ownerId, lockId );
			}
		}
		catch ( DeadlockLoserDataAccessException dle ) {
			LOG.debug( "Deadlock loser for lock {}", lockId, dle );
		}

		return acquired;
	}

	@Override
	public boolean isLocked( String lockId ) {
		checkDestroyed();
		return getLockOwner( lockId ) != null;
	}

	@Override
	public boolean isLockedByOwner( String ownerId, String lockId ) {
		Assert.notNull( ownerId, "ownerId must not be null" );
		checkDestroyed();
		return ownerId.equals( getLockOwner( lockId ) );
	}

	private String getLockOwner( String lockId ) {
		String ownerId = lockMonitor.getOwnerForLock( lockId );

		if ( ownerId == null ) {
			// Owner not found in current repository, dispatch to backend database
			LockInfo lockInfo = getLockInfo( lockId );

			if ( lockInfo != null ) {
				ownerId = lockInfo.getOwnerId();
			}
		}

		return ownerId;
	}

	private LockInfo getLockInfo( String lockId ) {
		try {
			return jdbcTemplate.queryForObject( sqlSelectLock,
			                                    new Object[] { lockId },
			                                    new LockInfoMapper() );
		}
		catch ( EmptyResultDataAccessException erdae ) {
			return null;
		}
		catch ( Exception e ) {
			throw new DistributedLockException( "Unable to fetch lock info for lock " + lockId, e );
		}
	}

	@Override
	public boolean verifyLockedByOwner( String ownerId, String lockId ) {
		checkDestroyed();
		try {
			return jdbcTemplate.update( sqlVerifyLock, System.currentTimeMillis(), lockId, ownerId ) == 1;
		}
		catch ( Exception e ) {
			throw new DistributedLockException( "Exception trying to update lock " + lockId, e );
		}
	}

	@Override
	public void release( DistributedLock lock ) {
		checkDestroyed();
		release( lock.getOwnerId(), lock.getKey() );
	}

	private void checkDestroyed() {
		if ( destroyed ) {
			throw new IllegalStateException(
					"The DistributedLockManager has been destroyed - creating locks is impossible." );
		}
	}

	private void release( String ownerId, String lockId ) {
		LOG.trace( "Owner {} is releasing lock {}", ownerId, lockId );
		lockMonitor.removeLock( ownerId, lockId );
		try {
			if ( jdbcTemplate.update( sqlReleaseLock, lockId, ownerId ) != 1 ) {
				LOG.trace( "Releasing lock {} failed - trying decreasing the holds", lockId );
				if ( jdbcTemplate.update( sqlDecreaseHold, lockId, ownerId ) != 1 ) {
					LOG.trace( "Releasing lock {} failed - possibly it was forcibly taken already", lockId );
				}
			}
		}
		catch ( DataAccessException dae ) {
			LOG.warn(
					"Clean release of lock {} in database failed - lock appears still taken but can be stolen after the idle time.",
					lockId );
		}
	}

	private static final class LockInfo
	{
		private String lockId, ownerId;
		private int holdCount;
		private long created, updated;

		public String getLockId() {
			return lockId;
		}

		public void setLockId( String lockId ) {
			this.lockId = lockId;
		}

		public String getOwnerId() {
			return ownerId;
		}

		public void setOwnerId( String ownerId ) {
			this.ownerId = ownerId;
		}

		public long getCreated() {
			return created;
		}

		public void setCreated( long created ) {
			this.created = created;
		}

		public long getUpdated() {
			return updated;
		}

		public void setUpdated( long updated ) {
			this.updated = updated;
		}

		public int getHoldCount() {
			return holdCount;
		}

		public void setHoldCount( int holdCount ) {
			this.holdCount = holdCount;
		}
	}

	private static final class LockInfoMapper implements RowMapper<LockInfo>
	{
		@Override
		public LockInfo mapRow( ResultSet rs, int rowNum ) throws SQLException {
			LockInfo lockInfo = new LockInfo();
			lockInfo.setLockId( rs.getString( "lock_id" ) );
			lockInfo.setOwnerId( rs.getString( "owner_id" ) );
			lockInfo.setCreated( rs.getLong( "created" ) );
			lockInfo.setUpdated( rs.getLong( "updated" ) );
			lockInfo.setHoldCount( rs.getInt( "holds" ) );

			return lockInfo;
		}
	}
}
