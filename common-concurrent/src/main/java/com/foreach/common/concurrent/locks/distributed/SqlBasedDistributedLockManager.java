package com.foreach.common.concurrent.locks.distributed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
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
 * Implementation of a {@link DistributedLockManager}
 * that uses a relational dbms as backend for synchronizing the lock access.
 * <p/>
 * Includes the monitor implementation that notifies the central lock repository on
 * which locks are still being used, as well as the cleanup thread that deletes old
 * unused locks from the database.
 */
public class SqlBasedDistributedLockManager implements DistributedLockManager
{
	private static final Logger LOG = LoggerFactory.getLogger( SqlBasedDistributedLockManager.class );

	private static final String SQL_TAKE_LOCK = "UPDATE %s " +
			"SET owner_id = ?, created = ?, updated = ? " +
			"WHERE lock_id = ? AND (owner_id IS NULL OR owner_id = ?)";
	private static final String SQL_STEAL_LOCK = "UPDATE %s " +
			"SET owner_id = ?, created = ?, updated = ? " +
			"WHERE lock_id = ? AND (owner_id IS NULL OR (owner_id = ? AND updated = ?))";

	private static final String SQL_SELECT_LOCK = "SELECT lock_id, owner_id, created, updated " +
			"FROM %s " +
			"WHERE lock_id = ?";
	private static final String SQL_INSERT_LOCK = "INSERT INTO %s (lock_id, owner_id, created, updated) " +
			"VALUES (?,?,?,?)";
	private static final String SQL_RELEASE_LOCK = "UPDATE %s " +
			"SET owner_id = NULL " +
			"WHERE lock_id = ? AND owner_id = ?";
	private static final String SQL_VERIFY_LOCK = "UPDATE %s " +
			"SET updated = ? " +
			"WHERE lock_id = ? AND owner_id = ?";
	private static final String SQL_CLEANUP = "DELETE FROM %s WHERE owner_id IS NULL AND updated < ?";

	private final String sqlTakeLock, sqlStealLock, sqlSelectLock, sqlInsertLock, sqlReleaseLock, sqlVerifyLock,
			sqlCleanup;

	private final ScheduledExecutorService monitorThread = Executors.newSingleThreadScheduledExecutor();

	private final SqlBasedDistributedLockConfiguration configuration;
	private final JdbcTemplate jdbcTemplate;
	private final SqlBasedDistributedLockMonitor lockMonitor;

	private boolean destroyed = false;

	public SqlBasedDistributedLockManager( DataSource dataSource, SqlBasedDistributedLockConfiguration configuration ) {
		this.configuration = configuration;

		jdbcTemplate = new JdbcTemplate( dataSource );
		lockMonitor = new SqlBasedDistributedLockMonitor( this );

		monitorThread.scheduleWithFixedDelay( lockMonitor, configuration.getVerifyInterval(),
		                                      configuration.getVerifyInterval(), TimeUnit.MILLISECONDS );
		monitorThread.scheduleWithFixedDelay( new CleanupMonitor(), 0, configuration.getCleanupInterval(),
		                                      TimeUnit.MILLISECONDS );

		sqlTakeLock = sql( SQL_TAKE_LOCK );
		sqlStealLock = sql(SQL_STEAL_LOCK );
		sqlSelectLock = sql(SQL_SELECT_LOCK);
		sqlInsertLock = sql(SQL_INSERT_LOCK);
		sqlReleaseLock = sql(SQL_RELEASE_LOCK);
		sqlVerifyLock = sql(SQL_VERIFY_LOCK);
		sqlCleanup = sql(SQL_CLEANUP);
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
				LOG.warn( "Exception trying to cleanup unused locks", e );
			}
		}
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

		return tryAcquire( lockId, ownerId, lock );
	}

	private boolean tryAcquire( String lockId, String ownerId, DistributedLock lock ) {
		boolean acquired = false;

		LOG.trace( "Owner {} is trying to acquire lock {}", ownerId, lockId );

		long timestamp = System.currentTimeMillis();
		int updated = jdbcTemplate.update( sqlTakeLock, ownerId, timestamp, timestamp, lockId, ownerId );

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

		return acquired;
	}

	@Override
	public boolean isLocked( String lockId ) {
		checkDestroyed();
		return getLockOwner( lockId ) != null;
	}

	@Override
	public boolean isLockedByOwner( String ownerId, String lockId ) {
		Assert.notNull( ownerId );
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
	}

	@Override
	public boolean verifyLockedByOwner( String ownerId, String lockId ) {
		checkDestroyed();
		return jdbcTemplate.update( sqlVerifyLock, System.currentTimeMillis(), lockId, ownerId ) == 1;
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
		if ( jdbcTemplate.update( sqlReleaseLock, lockId, ownerId ) != 1 ) {
			LOG.trace( "Releasing lock {} failed - possibly it was forcibly taken already", lockId );
		}
	}

	private static final class LockInfo
	{
		private String lockId, ownerId;
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

			return lockInfo;
		}
	}
}
