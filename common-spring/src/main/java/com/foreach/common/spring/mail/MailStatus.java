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

/**
 * Status object to check if mail has been sent.
 */
public final class MailStatus
{
	private final boolean mailSent;
	private final Throwable exception;

	public MailStatus( boolean mailSent ) {
		this( mailSent, null );
	}

	public MailStatus( boolean mailSent, Throwable exception ) {
		this.mailSent = mailSent;
		this.exception = exception;
	}

	/**
	 * If false then the exception causing mail to fail can be found
	 * using getException().  If any exception occurred.
	 *
	 * @return True if mail sent ok, false otherwise.
	 */
	public boolean isMailSent() {
		return mailSent;
	}

	/**
	 * @return Exception if one has been thrown.
	 */
	public Throwable getException() {
		return exception;
	}
}
