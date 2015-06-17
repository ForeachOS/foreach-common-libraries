package com.foreach.common.concurrent.locks.distributed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Monitor to be executed at regular intervals that will check if the
 * registered locks are still valid.
 *
 * @author Arne Vandamme
 */
public class SqlBasedDistributedLockMonitor implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( SqlBasedDistributedLockMonitor.class );

	private final SqlBasedDistributedLockManager lockManager;
	private final Map<ActiveLock, DistributedLock> activeLocks = new HashMap<>();
	private final long maxTimeBeforeUnstable;
	private final long maxCacheTime;

	public SqlBasedDistributedLockMonitor( SqlBasedDistributedLockManager lockManager,
	                                       long maxTimeBeforeUnstable,
	                                       long maxCacheTime ) {
		this.lockManager = lockManager;
		this.maxTimeBeforeUnstable = maxTimeBeforeUnstable;
		this.maxCacheTime = maxCacheTime;
	}

	public synchronized void addLock( String ownerId, DistributedLock lock ) {
		String lockId = lock.getKey();
		String existingOwnerId = getOwnerForLock( lockId );

		if ( existingOwnerId != null ) {
			// This guy just had his lock stolen
			reportStolen( existingOwnerId, lockId );
		}

		activeLocks.put( new ActiveLock( ownerId, lockId ), lock );
	}

	@Override
	public void run() {
		try {
			for ( Map.Entry<ActiveLock, DistributedLock> activeLock : getActiveLocks().entrySet() ) {
				ActiveLock key = activeLock.getKey();

				// Before checking, ensure that it is still supposed to be active
				if ( activeLocks.containsKey( key ) ) {
					LOG.trace( "Verifying lock {} is still owned by {}", key.getLockId(), key.getOwnerId() );

					// If not active, report stolen
					if ( !verifyStillLocked( key, activeLock.getValue() ) ) {
						reportStolen( key.getOwnerId(), key.getLockId() );
					}
				}
			}
		}
		catch ( Exception e ) {
			LOG.error( "Exception trying to monitor locks", e );
		}
	}

	/**
	 * In case something goes wrong, the monitor assumes the state is unchanged but sends an unstable
	 * callback if the lock state cannot be verified for too long
	 */
	private boolean verifyStillLocked( ActiveLock monitorInfo, DistributedLock original ) {
		boolean lockedByOwner = true;

		try {
			lockedByOwner = lockManager.verifyLockedByOwner( monitorInfo.getOwnerId(), monitorInfo.getLockId() );
			monitorInfo.setLastVerified( System.currentTimeMillis() );
		}
		catch ( DistributedLockException dle ) {
			LOG.warn( "Unable to update lock {} - lock might be unstable", monitorInfo.getLockId() );
			if ( isUnstable( monitorInfo ) ) {
				LOG.error( "Lock verification failed too many times - triggering lock unstable callback" );
				reportUnstable( monitorInfo.getLastVerified(), original, dle );
			}
		}

		return lockedByOwner;
	}

	private void reportUnstable( long lastVerified, DistributedLock lock, DistributedLockException dle ) {
		DistributedLock.LockUnstableCallback callback = lock.getUnstableCallback();

		if ( callback == null ) {
			callback = lockManager.getDefaultLockUnstableCallback();
		}

		if ( callback != null ) {
			callback.unstable( lock.getKey(), lock.getOwnerId(), lock, lastVerified, dle );
		}
	}

	private void reportStolen( String ownerId, String lockId ) {
		// Remove the lock from the monitor -  it's possible this has happened already in the meantime
		DistributedLock removedLock = removeLock( ownerId, lockId );

		if ( removedLock != null ) {
			LOG.trace( "Lock {} was supposed to be owned by {}, but it appears to be stolen",
			           lockId, ownerId );

			// Execute the stolen callback if there is one
			DistributedLock.LockStolenCallback callback = removedLock.getStolenCallback();

			if ( callback == null ) {
				callback = lockManager.getDefaultLockStolenCallback();
			}

			if ( callback != null ) {
				callback.stolen( lockId, ownerId, removedLock );
			}
		}
	}

	public synchronized DistributedLock removeLock( String ownerId, String lockId ) {
		return activeLocks.remove( new ActiveLock( ownerId, lockId ) );
	}

	public synchronized Map<ActiveLock, DistributedLock> getActiveLocks() {
		return new HashMap<>( activeLocks );
	}

	/**
	 * Get the lock owner according to the monitor thread.  The monitor caches the active locks for
	 * performance.  If this method returns null, it simply means the monitor cannot reliably tell who the owner is.
	 */
	public synchronized String getOwnerForLock( String lockId ) {
		for ( ActiveLock activeLock : activeLocks.keySet() ) {
			if ( activeLock.getLockId().equals( lockId ) && isReliable( activeLock ) ) {
				return activeLock.getOwnerId();
			}
		}

		return null;
	}

	private boolean isUnstable( ActiveLock activeLock ) {
		return System.currentTimeMillis() - activeLock.getLastVerified() > maxTimeBeforeUnstable;
	}

	private boolean isReliable( ActiveLock activeLock ) {
		return System.currentTimeMillis() - activeLock.getLastVerified() <= maxCacheTime;
	}

	public static class ActiveLock
	{
		private String ownerId, lockId;
		private long lastVerified;

		ActiveLock( String ownerId, String lockId ) {
			this.ownerId = ownerId;
			this.lockId = lockId;
			lastVerified = System.currentTimeMillis();
		}

		public String getOwnerId() {
			return ownerId;
		}

		public String getLockId() {
			return lockId;
		}

		long getLastVerified() {
			return lastVerified;
		}

		void setLastVerified( long lastVerified ) {
			this.lastVerified = lastVerified;
		}

		@Override
		public boolean equals( Object o ) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			ActiveLock that = (ActiveLock) o;

			return Objects.equals( lockId, that.lockId ) && Objects.equals( ownerId, that.ownerId );
		}

		@Override
		public int hashCode() {
			return Objects.hash( ownerId, lockId );
		}
	}
}
