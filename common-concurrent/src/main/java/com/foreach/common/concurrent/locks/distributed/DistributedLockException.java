package com.foreach.common.concurrent.locks.distributed;

/**
 * @author Arne Vandamme
 */
public class DistributedLockException extends RuntimeException
{
	public DistributedLockException() {
	}

	public DistributedLockException( String message ) {
		super( message );
	}

	public DistributedLockException( String message, Throwable cause ) {
		super( message, cause );
	}

	public DistributedLockException( Throwable cause ) {
		super( cause );
	}

	public DistributedLockException( String message,
	                                 Throwable cause,
	                                 boolean enableSuppression,
	                                 boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
	}
}
