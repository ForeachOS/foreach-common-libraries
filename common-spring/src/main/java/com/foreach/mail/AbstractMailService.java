package com.foreach.mail;

import com.foreach.validators.MultipleEmailsValidator;
import org.apache.log4j.Logger;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import java.util.Map;


public abstract class AbstractMailService implements MailService
{
	abstract JavaMailSender getMailSender();

	abstract String getOriginator();

	abstract String getServiceBccRecipient();

	abstract Logger getLogger( );


	private MimeMessage createMimeMessage( String from, String to, String bccs,
	                                       String subject, String body, Map<String,File> attachments )
			throws MessagingException
	{
		MimeMessage message = getMailSender().createMimeMessage();

		// inform the MessageHelper on the multipartness of our message
		MimeMessageHelper helper = new MimeMessageHelper( message, attachments!=null );

		helper.setTo( getToAddresses( to ) );
		helper.setFrom( ( from == null ) ? getOriginator() : from );
		helper.setText( body, true );
		message.setSubject( subject );

		String bccRecipients = ( bccs == null )? getServiceBccRecipient() : bccs;

		if ( bccRecipients != null ) {
			helper.setBcc( getToAddresses( bccRecipients ) );
		}

		if ( attachments != null ) {
			for( Map.Entry<String,File> entry : attachments.entrySet())
			helper.addAttachment( entry.getKey(), entry.getValue() );
		}

		return message;
	}

	public final boolean sendMimeMail( String from, String to, String bccs,
	                                   String subject, String body, Map<String,File> attachments )
	{
		try {

			MimeMessage message = createMimeMessage( from, to, bccs, subject, body, attachments );

			getLogger().info( "Sending html email " + from + " > " + to + ": " + subject );
			getMailSender().send( message );

		} catch ( MessagingException e ) {
			getLogger().error( "Failed to compose mail", e );
			return false;
		} catch ( MailException e) {
			getLogger().error( "Failed to send mail", e );
			return false;
		} catch ( Exception e ) {
			getLogger().error( "Failed to send mail", e );
			return false;
		}

		return true;
	}

	private String[] getToAddresses( String to )
	{
		List<String> emailList = MultipleEmailsValidator.separateEmailAddresses( to );

		return emailList.toArray(new String[emailList.size()]);
	}

}
