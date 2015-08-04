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
package com.foreach.common.spring.mail;

import com.foreach.common.concurrent.SynchronousTaskExecutor;
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
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestBasicMailService
{
	private BasicMailService mailService;

	private JavaMailSender mailSender;
	private String originator;
	private String serviceBccRecipients;

	private String from;
	private String to;
	private String subject;
	private String body;

	@Before
	public void prepareForTest() {
		originator = "testMailService@serverkot.be";
		serviceBccRecipients = "bigbrother@serverkot.be";

		mailSender = mock( JavaMailSender.class );

		mailService = new BasicMailService();
		mailService.setOriginator( originator );
		mailService.setJavaMailSender( mailSender );
		mailService.setServiceBccRecipients( serviceBccRecipients );

		from = "someone@foreach.be";
		to = "someone_else@foreach.be";
		subject = "hello subject";
		body = "<html><body>hello body</body></html>";
	}

	@Test
	public void simpleMail() throws Exception {
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
	public void testMultipleRecipients() throws MessagingException {
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
	public void testAttachments() throws MessagingException, IOException {
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
	public void testCustomExecutorService() throws Exception {
		mailService.setExecutorService( new ScheduledThreadPoolExecutor( 2 ) );

		MimeMessage message = new MimeMessage( Session.getInstance( new Properties() ) );

		when( mailSender.createMimeMessage() ).thenReturn( message );

		Future<MailStatus> future = mailService.sendMimeMail( from, to, null, subject, body, null );

		assertTrue( future.get().isMailSent() );
	}

	@Test
	public void exceptionDoesNotGetEatenWithSyncExecutor() throws Exception {
		mailService.setExecutorService( new SynchronousTaskExecutor() );

		RuntimeException expected = new RuntimeException();

		when( mailSender.createMimeMessage() ).thenReturn( new MimeMessage( Session.getInstance( new Properties() ) ) );
		doThrow( expected ).when( mailSender ).send( (MimeMessage) anyObject() );

		Future<MailStatus> future = mailService.sendMimeMail( "from", "to", "", "subject", "body", null );
		MailStatus status = future.get();

		assertFalse( status.isMailSent() );
		assertSame( expected, status.getException() );
	}

	@Test
	public void exceptionDoesNotGetEatenWithAsync() throws Exception {
		mailService.setExecutorService( new ScheduledThreadPoolExecutor( 2 ) );

		RuntimeException expected = new RuntimeException();

		when( mailSender.createMimeMessage() ).thenReturn( new MimeMessage( Session.getInstance( new Properties() ) ) );
		doThrow( expected ).when( mailSender ).send( (MimeMessage) anyObject() );

		Future<MailStatus> future = mailService.sendMimeMail( "from", "to", "", "subject", "body", null );

		MailStatus status = future.get();

		assertFalse( status.isMailSent() );
		assertSame( expected, status.getException() );
	}

	private InternetAddress[] adressesFromStrings( String s[] ) throws AddressException {
		InternetAddress addresses[] = new InternetAddress[s.length];

		for ( int i = 0; i < s.length; i++ ) {
			addresses[i] = new InternetAddress( s[i] );
		}

		return addresses;
	}

	private String condense( String s[] ) {
		StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < s.length; i++ ) {
			if ( i > 0 ) {
				sb.append( ";" );
			}
			sb.append( s[i] );
		}
		return sb.toString();
	}
}
