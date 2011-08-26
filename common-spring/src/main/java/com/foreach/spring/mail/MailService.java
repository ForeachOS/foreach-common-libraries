package com.foreach.spring.mail;

import java.io.File;
import java.util.Map;

public interface MailService
{
	/**
	 * Send a mail message with optional attachments.
	 *
	 * @param from        the email address of the originator.
	 * @param to          the email address(es) of the intended recipient(s).
	 * @param bccs        the email address(es) of other intended recipient(s).
	 * @param subject     the subject of the mail message.
	 * @param body        the body of the mail message.
	 * @param attachments a map of included files.
	 * @return true if no error occurred sending the message. The exact semantics are implementation-dependent,
	 *         usually it means the message was successfully delivered to the MSA or MTA.
	 * @see <a href="http://tools.ietf.org/html/rfc2476">RFC 2476</a>.
	 */
	boolean sendMimeMail(
			String from, String to, String bccs, String subject, String body, Map<String, File> attachments );
}
