package com.foreach.spring.mail;

/**
 * Status object to check if mail has been sent.
 */
public final class MailStatus
{
	private final boolean mailSent;
	private final Throwable exception;

	public MailStatus( boolean mailSent ) {
		this( mailSent, null );
	}

	public MailStatus( boolean mailSent, Throwable exception ) {
		this.mailSent = mailSent;
		this.exception = exception;
	}

	/**
	 * If false then the exception causing mail to fail can be found
	 * using getException().  If any exception occurred.
	 *
	 * @return True if mail sent ok, false otherwise.
	 */
	public boolean isMailSent() {
		return mailSent;
	}

	/**
	 * @return Exception if one has been thrown.
	 */
	public Throwable getException() {
		return exception;
	}
}
