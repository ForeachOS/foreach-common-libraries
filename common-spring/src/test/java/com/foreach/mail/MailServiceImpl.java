package com.foreach.mail;

import com.foreach.mail.AbstractMailService;
import com.foreach.mail.MailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.apache.log4j.Logger;

public class MailServiceImpl extends AbstractMailService implements MailService
{
	private JavaMailSender mailSender;

	private String originator;

	private String serviceBccRecipient;

	private Logger logger;


	public void setMailSender( JavaMailSender mailSender )
	{
		this.mailSender = mailSender;
	}

	public final JavaMailSender getMailSender()
	{
		return mailSender;
	}

	public final void setOriginator( String originator )
	{
		this.originator = originator;
	}

	public final String getOriginator()
	{
		return originator;
	}

	public final void setServiceBccRecipient( String serviceBccRecipient )
	{
		this.serviceBccRecipient = serviceBccRecipient;
	}

	public final String getServiceBccRecipient()
	{
		return serviceBccRecipient;
	}

	public final void setLogger( Logger logger )
	{
		this.logger = logger;
	}

	public final Logger getLogger( )
	{
		return logger;
	}
}
