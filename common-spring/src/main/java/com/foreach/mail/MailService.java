package com.foreach.mail;

import java.io.File;
import java.util.Map;

public interface MailService
{
	boolean sendMimeMail( String from, String to, String bccs, String subject, String body, Map<String,File> attachments );
}
