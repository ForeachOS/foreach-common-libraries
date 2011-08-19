package com.foreach.mail;

import org.springframework.mail.javamail.JavaMailSender;

import org.junit.Before;
import org.junit.Test;

import org.apache.log4j.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class TestMailService
{
	private MailService mailService;

	private JavaMailSender mailSender;
	private String originator;
	private String serviceBccRecipients;
	private Logger logger;

	private String from;
	private String to;
	private String subject;
	private String body;

	@Before
	public void prepareForTest()
	{
		originator = "testMailService@serverkot.be";
		serviceBccRecipients = "bigbrother@serverkot.be";

		mailSender = mock( JavaMailSender.class );
		logger = mock( Logger.class );

		MailServiceImpl mailServiceImpl = new MailServiceImpl();
		mailServiceImpl.setOriginator( originator );
		mailServiceImpl.setMailSender( mailSender );
		mailServiceImpl.setServiceBccRecipients( serviceBccRecipients );
		mailServiceImpl.setLogger( logger );

		mailService = mailServiceImpl;

		from = "someone@foreach.be";
		to = "someone@foreach.be";
		subject = "hello subject";
		body = "<html><body>hello body</body></html>";
	}

	@Test
	public void simpleMail() throws Exception
	{
		MimeMessage mockMessage = mock( MimeMessage.class );

		InternetAddress fromAddress = new InternetAddress(from);
		InternetAddress toAddress = new InternetAddress(to);
		InternetAddress toAddresses[] = { toAddress };

		when( mailSender.createMimeMessage() ).thenReturn( mockMessage );

		mailService.sendMimeMail( from, to, null, subject, body, null );

		verify( mailSender ).send( mockMessage );

		verify( mockMessage ).setFrom( fromAddress );
		verify( mockMessage ).setSubject( subject );
		verify( mockMessage ).setRecipients( MimeMessage.RecipientType.TO, toAddresses );
	}

	@Test
	public void testMultipleRecipients() throws MessagingException
	{
		String tos[] = {"foo1@foreach.com","foo2@foreach.com","foo3@foreach.com"};
		String bccs[] = {"foo4@foreach.com","foo5@foreach.com","foo6@foreach.com"};

		InternetAddress toAddresses[] = adressesFromStrings(tos);
		InternetAddress bccAddresses[] = adressesFromStrings(bccs);

		MimeMessage mockMessage = mock( MimeMessage.class );

		when( mailSender.createMimeMessage() ).thenReturn( mockMessage );

		mailService.sendMimeMail( from, condense(tos), condense(bccs), subject, body, null );

		verify( mailSender ).send( mockMessage );

		verify( mockMessage ).setFrom( new InternetAddress(from) );
		verify( mockMessage ).setSubject( subject );
		verify( mockMessage ).setRecipients( MimeMessage.RecipientType.TO, toAddresses );
		verify( mockMessage ).setRecipients( MimeMessage.RecipientType.BCC, bccAddresses );
	}

	private InternetAddress[] adressesFromStrings( String s[] ) throws AddressException
	{
		InternetAddress addresses[] = new InternetAddress[s.length];

		for(int i = 0; i< s.length; i++) {
			addresses[i] = new InternetAddress( s[i] );
		}

		return addresses;
	}

	private String condense(String s[])
	{
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i< s.length; i++) {
			if(i>0)
				sb.append( ";" );
			sb.append( s[i] );
		}
		return sb.toString();
	}
}
