package com.foreach.test.spring.mail;

import com.foreach.spring.concurrent.AsynchronousTaskExecutor;
import com.foreach.spring.mail.BasicMailService;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.*;

public class TestBasicMailService
{
	private BasicMailService mailService;

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

		mailService = new BasicMailService();
		mailService.setOriginator( originator );
		mailService.setJavaMailSender( mailSender );
		mailService.setServiceBccRecipients( serviceBccRecipients );
		mailService.setLogger( logger );

		from = "someone@foreach.be";
		to = "someone_else@foreach.be";
		subject = "hello subject";
		body = "<html><body>hello body</body></html>";
	}

	@Test
	public void simpleMail() throws Exception
	{
		MimeMessage message = new MimeMessage( Session.getInstance( new Properties() ) );

		InternetAddress fromAddress = new InternetAddress( from );
		InternetAddress toAddress = new InternetAddress( to );

		when( mailSender.createMimeMessage() ).thenReturn( message );

		mailService.sendMimeMail( from, to, null, subject, body, null );

		verify( mailSender ).send( message );

		Assert.assertEquals( subject, message.getSubject() );
		Assert.assertEquals( fromAddress, message.getFrom()[0] );
		Assert.assertEquals( toAddress, message.getAllRecipients()[0] );
	}

	@Test
	public void testMultipleRecipients() throws MessagingException
	{
		String tos[] = { "foo1@foreach.com", "foo2@foreach.com", "foo3@foreach.com" };
		String bccs[] = { "foo4@foreach.com", "foo5@foreach.com", "foo6@foreach.com" };

		InternetAddress fromAddress = new InternetAddress( from );
		InternetAddress toAddresses[] = adressesFromStrings( tos );
		InternetAddress bccAddresses[] = adressesFromStrings( bccs );

		MimeMessage message = new MimeMessage( Session.getInstance( new Properties() ) );

		when( mailSender.createMimeMessage() ).thenReturn( message );

		mailService.sendMimeMail( from, condense( tos ), condense( bccs ), subject, body, null );

		verify( mailSender ).send( message );

		Assert.assertEquals( subject, message.getSubject() );
		Assert.assertEquals( fromAddress, message.getFrom()[0] );

		// this could be rewritten...

		Assert.assertEquals( toAddresses[0], message.getAllRecipients()[0] );
		Assert.assertEquals( toAddresses[1], message.getAllRecipients()[1] );
		Assert.assertEquals( toAddresses[2], message.getAllRecipients()[2] );

		Assert.assertEquals( bccAddresses[0], message.getAllRecipients()[3] );
		Assert.assertEquals( bccAddresses[1], message.getAllRecipients()[4] );
		Assert.assertEquals( bccAddresses[2], message.getAllRecipients()[5] );
	}

	@Test
	public void testAttachments() throws MessagingException, IOException
	{
		MimeMessage message = new MimeMessage( Session.getInstance( new Properties() ) );

		when( mailSender.createMimeMessage() ).thenReturn( message );

		Map<String, File> files = new LinkedHashMap<String, File>();

		File file1 = new File( "path1" );
		File file2 = new File( "path2" );

		files.put( "f1", file1 );
		files.put( "f2", file2 );

		mailService.sendMimeMail( from, to, null, subject, body, files );

		verify( mailSender ).send( message );

		Multipart mp = (Multipart) message.getContent();

		// also a bit messy

		Assert.assertEquals( 3, mp.getCount() );

		Assert.assertEquals( "f1", mp.getBodyPart( 1 ).getFileName() );
		Assert.assertEquals( "f2", mp.getBodyPart( 2 ).getFileName() );
	}

	@Test
	public void testCustomExecutorService()
	{
		mailService.setExecutorService( new AsynchronousTaskExecutor() );

		MimeMessage message = new MimeMessage( Session.getInstance( new Properties() ) );

		when( mailSender.createMimeMessage() ).thenReturn( message );

		mailService.sendMimeMail( from, to, null, subject, body, null );
	}

	private InternetAddress[] adressesFromStrings( String s[] ) throws AddressException
	{
		InternetAddress addresses[] = new InternetAddress[s.length];

		for ( int i = 0; i < s.length; i++ ) {
			addresses[i] = new InternetAddress( s[i] );
		}

		return addresses;
	}

	private String condense( String s[] )
	{
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < s.length; i++ ) {
			if ( i > 0 ) {
				sb.append( ";" );
			}
			sb.append( s[i] );
		}
		return sb.toString();
	}
}
