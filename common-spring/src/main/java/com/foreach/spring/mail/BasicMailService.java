package com.foreach.spring.mail;

import com.foreach.spring.concurrent.PreComputedFuture;
import com.foreach.spring.concurrent.SynchronousTaskExecutor;
import com.foreach.spring.validators.MultipleEmailsValidator;
import org.apache.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * MailService sends smtp mails with optional attachments.
 * <p/>
 * By default, a MailService instance will send mails synchronously,
 * but you can alter this behaviour by changing the executorService.
 * <p/>
 * If you want the option of sending mails both synchronously and asynchronously,
 * you should create two MailService instances.
 * <p/>
 * In most cases, you will have the BasicMailService configured as a bean in an xml file.
 * If you want to use annotation in combination with a component scanner,
 * you have to subclass BasicMailService and annotate the subclass.
 * <p/>
 * Example spring configuration with a shared javaMailSender and a private asynchronous executorService:
 * <pre>
 * {@code
 *  <bean id="sharedMailSender" class="org.springframework.mail.javamail.JavaMailSenderImpl">
 *      <property name="host" value="localhost"/>
 *      <property name="defaultEncoding" value="UTF-8"/>
 *      <property name="javaMailProperties">
 *          <props>
 *              <prop key="mail.smtp.connectiontimeout">30000</prop>
 *              <prop key="mail.smtp.timeout">30000</prop>
 *          </props>
 *      </property>
 *  </bean>
 *
 *  <bean id="mailService" class="com.foreach.spring.mail.BasicMailService">
 *      <property name="originator" value="noreply@foo.bar"/>
 *      <property name="executorService">
 *          <bean class="java.util.concurrent.ScheduledThreadPoolExecutor" destroy-method="shutdown">
 *              <constructor-arg index="0" type="int" value="2"/>
 *          </bean>
 *      </property>
 *      <property name="javaMailSender" ref="sharedMailSender"/>
 *  </bean>
 * }
 * </pre>
 */
public class BasicMailService implements MailService
{
	private Logger logger = Logger.getLogger( getClass() );

	private JavaMailSender javaMailSender;
	private String originator;
	private String serviceBccRecipients;

	// default to synchronous operation.
	private ExecutorService executorService = new SynchronousTaskExecutor();

	protected final void setLogger( Logger logger )
	{
		this.logger = logger;
	}

	/**
	 * Get the logger
	 *
	 * @return Logger
	 */
	protected final Logger getLogger()
	{
		return this.logger;
	}

	/**
	 * Set the JavaMailSender to be used.
	 */
	public final void setJavaMailSender( JavaMailSender javaMailSender )
	{
		this.javaMailSender = javaMailSender;
	}

	/**
	 * Set the default originator to be used to send messages through the javaMailSender.
	 *
	 * @param originator the email address of the default originator.
	 *                   This value can be overridden on a per message basis.
	 */
	public final void setOriginator( String originator )
	{
		this.originator = originator;
	}

	/**
	 * Set the default bcc: recipients for this service.
	 *
	 * @param serviceBccRecipients a comma or semicolon separated list of email adresses.
	 *                             This value can be overridden on a per message basis.
	 */
	public final void setServiceBccRecipients( String serviceBccRecipients )
	{
		this.serviceBccRecipients = serviceBccRecipients;
	}

	public final String getServiceBccRecipients()
	{
		return serviceBccRecipients;
	}

	/**
	 * Set the executorService used to send messages through the javaMailSender.
	 *
	 * @param executorService By default, a synchronous TaskExecutoService is configured.
	 */
	public final synchronized void setExecutorService( ExecutorService executorService )
	{
		this.executorService = executorService;
	}

	/**
	 * Get the current ExecutorService being used.
	 */
	public final synchronized ExecutorService getExecutorService()
	{
		return executorService;
	}

	/**
	 * Send a mail message with optional attachments.
	 *
	 * @param from        the email address of the originator.
	 * @param to          the email address(es) of the intended recipient(s).
	 * @param bccs        the email address(es) of other intended recipient(s).
	 * @param subject     the subject of the mail message.
	 * @param body        the body of the mail message.
	 * @param attachments a map of included files.
	 * @return A future containing the MailStatus object.  The status of sending is dependent on the actual MailSender used.
	 *         If success it usually means the message was successfully delivered to the MSA or MTA.
	 * @see <a href="http://tools.ietf.org/html/rfc2476">RFC 2476</a>.
	 */
	public final Future<MailStatus> sendMimeMail(
			String from, String to, String bccs, String subject, String body, Map<String, File> attachments )
	{
		final Logger log = getLogger();

		try {

			final MimeMessage message = createMimeMessage( from, to, bccs, subject, body, attachments );

			log.info( "Sending html email " + from + " > " + to + ": " + subject );

			return sendMail( message );
		}
		catch ( MessagingException e ) {
			log.error( "Failed to compose mail ", e );
			return new PreComputedFuture<MailStatus>( new MailStatus( false, e ) );
		}
	}

	private MimeMessage createMimeMessage(
			String from,
			String to,
			String bccs,
			String subject,
			String body,
			Map<String, File> attachments ) throws MessagingException
	{
		MimeMessage message = javaMailSender.createMimeMessage();

		// inform the MessageHelper on the multipartness of our message
		MimeMessageHelper helper = new MimeMessageHelper( message, attachments != null );

		helper.setTo( getToAddresses( to ) );
		helper.setFrom( ( from == null ) ? originator : from );
		helper.setText( body, true );
		message.setSubject( subject );

		String bccRecipients = ( bccs == null ) ? serviceBccRecipients : bccs;

		if ( bccRecipients != null ) {
			helper.setBcc( getToAddresses( bccRecipients ) );
		}

		if ( attachments != null ) {
			for ( Map.Entry<String, File> entry : attachments.entrySet() ) {
				helper.addAttachment( entry.getKey(), entry.getValue() );
			}
		}

		return message;
	}

	private Future<MailStatus> sendMail( final MimeMessage message )
	{
		return getExecutorService().submit( new Callable<MailStatus>()
		{
			public MailStatus call() throws Exception
			{
				try {
					javaMailSender.send( message );

					return new MailStatus( true );
				}
				catch ( Exception e ) {
					getLogger().error( "Failed to send message ", e );
					return new MailStatus( false, e );
				}
			}
		} );
	}

	private String[] getToAddresses( String to )
	{
		List<String> emailList = MultipleEmailsValidator.separateEmailAddresses( to );

		return emailList.toArray( new String[emailList.size()] );
	}
}
