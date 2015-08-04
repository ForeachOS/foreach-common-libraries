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

import java.io.File;
import java.util.Map;
import java.util.concurrent.Future;

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
	 * @return A future containing the MailStatus object.  The status of sending is dependent on the actual MailSender used.
	 *         If success it usually means the message was successfully delivered to the MSA or MTA.
	 * @see <a href="http://tools.ietf.org/html/rfc2476">RFC 2476</a>.
	 */
	Future<MailStatus> sendMimeMail( String from,
	                                 String to,
	                                 String bccs,
	                                 String subject,
	                                 String body,
	                                 Map<String, File> attachments );
}
