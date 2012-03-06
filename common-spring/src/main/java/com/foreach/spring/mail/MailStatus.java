package com.foreach.spring.mail;

public final class MailStatus
{
	private final boolean mailSent;
	private final Throwable exception;

	public MailStatus( boolean mailSent )
	{
		this( mailSent, null );
	}

	public MailStatus( boolean mailSent, Throwable exception )
	{
		this.mailSent = mailSent;
		this.exception = exception;
	}

	public boolean isMailSent()
	{
		return mailSent;
	}

	public Throwable getException()
	{
		return exception;
	}
}
