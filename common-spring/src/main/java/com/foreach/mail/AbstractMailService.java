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
	/**
	 * An org.springframework.mail.javamail.JavaMailSender.
	 */
	public abstract JavaMailSender getMailSender();

	/**
	 * The default originator of messages sent by the MailService when no explicit originator is provided.
	 */
	public abstract String getOriginator();

	/**
	 * If not null, a comma or semi-colon separated list of email adresses that receive a copy of each message sent.
	 * If other bcc recipients are specified for a specific message, they will override this field,
	 * so the bcc recipients are replaced instead of merged.
	 */
	public abstract String getServiceBccRecipients();

	/**
	 * An org.apache.log4j.Logger.
	 */
	protected abstract Logger getLogger( );


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

		String bccRecipients = ( bccs == null )? getServiceBccRecipients() : bccs;

		if ( bccRecipients != null ) {
			helper.setBcc( getToAddresses( bccRecipients ) );
		}

		if ( attachments != null ) {
			for( Map.Entry<String,File> entry : attachments.entrySet())
			helper.addAttachment( entry.getKey(), entry.getValue() );
		}

		return message;
	}

	/**
	 * Send a mail message with optional attachments.
	 *
	 * @param from the email address of the originator.
	 * @param to the email address(es) of the intended recipient(s).
	 * @param bccs the email address(es) of other intended recipient(s).
	 * @param subject the subject of the mail message.
	 * @param body the body of the mail message.
	 * @param attachments a map of included files.
	 *
	 * @return true if no error occurred sending the message. The exact semantics are dependent on the actual MailSender used,
	 * usually it means the message was successfully delivered to the MSA or MTA.
	 * @see <a href="http://tools.ietf.org/html/rfc2476">RFC 2476</a>.
	 */

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
