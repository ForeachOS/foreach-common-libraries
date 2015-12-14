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
package com.foreach.common.web.logging;

import com.foreach.common.spring.context.ApplicationContextInfo;
import com.foreach.common.spring.mail.MailService;
import com.foreach.common.web.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ExceptionToMailResolver sends a mail for every java exception
 * that is not caught at the controller level.
 * <p/>
 * To use this resolver, declare a bean for this class in your spring configuration file.
 * <p/>
 * Example spring configuration:
 * <p/>
 * <pre>
 *  &lt;bean class="com.foreach.web.logging.ExceptionToMailResolver"&gt;
 *      &lt;property name="fromAddress" value="${errormail.from}"/&gt;
 *      &lt;property name="toAddress" value="${erromail.to}"/&gt;
 *      &lt;property name="order" value="1"/&gt;
 *      &lt;property name="exceptionMappings"&gt;
 *          &lt;props&gt;
 *              &lt;prop key="java.lang.Throwable"&gt;error&lt;/prop&gt;
 *          &lt;/props&gt;
 *      &lt;/property&gt;
 *      &lt;property name="mailService" ref="mailService"/&gt;
 *      &lt;property name="applicationContextInfo" ref="applicationContext"/&gt;
 *  &lt;/bean&gt;
 * </pre>
 * <p/>
 * <p> When you create an instance, following properties are mandatory:</p>
 * <ul>
 * <li>fromAddress:
 * you may want to have a different sender for each application and environment combination to facilitate filtering,
 * so<br/>smurfvillage-staging-errors@foo.bar might be a better idea than noreply@foo.bar.</li>
 * <li>toAddress: usually the operators or developers.</li>
 * <li>order: see AbstractHandlerExceptionResolver</li>
 * <li>exceptionMappings: all the exceptions caught by this exception resolver. see AbstractHandlerExceptionResolver</li>
 * <li>mailService: the name of a MailService bean</li>
 * <li>applicationContextInfo: the name of the ApplicationContextInfo bean</li>
 * </ul>
 */
public class ExceptionToMailResolver extends SimpleMappingExceptionResolver
{
	private static final String TABLE_START_TAG =
			"<table border='1' cellpadding='3' style='font-family: tahoma;font-size: 12px;'>";

	private static final String TABLE_END_TAG = "</table>";

	private Logger logger = LoggerFactory.getLogger( getClass() );

	private String fromAddress, toAddress;

	private MailService mailService;

	private ApplicationContextInfo applicationContextInfo;

	private ExceptionPredicate exceptionPredicate = new DefaultExceptionPredicate();

	/**
	 * Specify your own custom logger
	 *
	 * @param logger
	 */
	protected final void setLogger( Logger logger ) {
		this.logger = logger;
	}

	/**
	 * Get the logger
	 *
	 * @return Logger
	 */
	protected final Logger getLogger() {
		return this.logger;
	}

	/**
	 * Specify from email address
	 *
	 * @param fromAddress
	 */
	public final void setFromAddress( String fromAddress ) {
		this.fromAddress = fromAddress;
	}

	/**
	 * Specify to email address
	 *
	 * @param toAddress
	 */
	public final void setToAddress( String toAddress ) {
		this.toAddress = toAddress;
	}

	/**
	 * set the mail service, which actually sends the exception mail
	 *
	 * @param mailService
	 */
	public final void setMailService( MailService mailService ) {
		this.mailService = mailService;
	}

	/**
	 * set the ApplicationContextInfo object holding the properties of current running application
	 *
	 * @param context
	 */
	public final void setApplicationContext( ApplicationContextInfo context ) {
		this.applicationContextInfo = context;
	}

	/**
	 * sending mail for certain exception is determined by evaluating the specified predicate
	 *
	 * @param exceptionPredicate
	 */
	public void setExceptionPredicate( ExceptionPredicate exceptionPredicate ) {
		Assert.notNull( exceptionPredicate );
		this.exceptionPredicate = exceptionPredicate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ModelAndView doResolveException( HttpServletRequest request,
	                                              HttpServletResponse response,
	                                              Object handler,
	                                              Exception ex ) {
		logger.error( "Exception has occured ", ex );

		try {
			boolean sendMail = ( ex != null && exceptionPredicate.evaluate( ex ) );
			if ( sendMail ) {
				String mailBody = createExceptionMailBody( request, handler, ex );
				String mailSubject = createExceptionMailSubject( ex );

				mailService.sendMimeMail( fromAddress, toAddress, null, mailSubject, mailBody, null );
			}
		}
		catch ( RuntimeException rex ) {
			logger.error( "New exception when handling exception ", rex );
		}

		return super.doResolveException( request, response, handler, ex );
	}

	private String createExceptionMailSubject( Exception ex ) {
		return new StringBuffer( "[" ).append( applicationContextInfo.getLabel() ).append( "-" ).append(
				applicationContextInfo.getApplicationName() ).append( " v" ).append(
				applicationContextInfo.getBuildNumber() ).append( "] " ).append( ex.getClass().toString() ).toString();
	}

	private String createExceptionMailBody( HttpServletRequest request, Object handler, Exception ex ) {
		DateFormat readableDate = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss" );

		Date now = new Date();

		StringWriter message = new StringWriter();
		PrintWriter html = new PrintWriter( message );

		// Write general params
		html.print( "<html><head></head><body style='font-family: tahoma;font-size: 12px;'>" );
		html.print( TABLE_START_TAG );

		String uniqueId =
				StringUtils.defaultIfBlank( (String) request.getAttribute( RequestLogInterceptor.ATTRIBUTE_UNIQUE_ID ),
				                            "unavailable" );

		writeParam( html, "request id", uniqueId + " (duration: " + getRequestDuration( request ) + ")" );

		writeParam( html, "date", readableDate.format( now ) );
		writeParam( html, "site",
		            applicationContextInfo.getLabel() + "-" + applicationContextInfo.getApplicationName() + " (" + applicationContextInfo.getEnvironment() + ")" );
		writeParam( html, "build",
		            "v" + applicationContextInfo.getBuildNumber() + " (build date: " + readableDate.format(
				            applicationContextInfo.getBuildDate() ) + ")" );
		writeParam( html, "uptime", DurationFormatUtils.formatDuration(
				now.getTime() - applicationContextInfo.getStartupDate().getTime(),
				"d'd' H'h' m'm'" ) + " (started: " + readableDate.format(
				applicationContextInfo.getStartupDate() ) + ")" );
		writeParam( html, "server", request.getServerName() );
		writeParam( html, "URL", request.getMethod() + " " + createUrlFromRequest( request ) );
		writeParam( html, "User-Agent", StringUtils.defaultIfBlank( request.getHeader( "User-Agent" ), "-" ) );
		writeParam( html, "Remote IP", StringUtils.defaultIfBlank( WebUtils.getRemoteAddress( request ), "-" ) );
		writeParam( html, "Referer", StringUtils.defaultIfBlank( request.getHeader( "Referrer" ), "-" ) );
		writeParam( html, "controller", handler != null ? handler.getClass() : "-" );

		String viewName =
				StringUtils.defaultIfBlank( (String) request.getAttribute( RequestLogInterceptor.ATTRIBUTE_VIEW_NAME ),
				                            "-" );

		writeParam( html, "view", viewName );

		writeParam( html, "user", request.getUserPrincipal() != null ? StringUtils.defaultIfBlank(
				request.getUserPrincipal().getName(), "-" ) : "-" );
		html.append( TABLE_END_TAG );

		// Write message
		html.append( "<h5>Message</h5>" );
		html.append( "<p><pre style='font-family: tahoma;font-size: 12px;'>" ).append( ex.getMessage() ).append(
				"</pre></p>" );

		// Write stack trace
		html.append( "<h5>Stack trace</h5>" );
		html.append( "<p><pre style='font-family: tahoma;font-size: 12px;'>" );
		ex.printStackTrace( html );
		html.append( "</pre></p>" );

		writeRequestParameters( html, request );

		writeRequestHeaders( html, request );

		writeCookies( html, request );

		writeRequestAttributes( html, request );

		writeSessionAttributes( html, request );

		html.print( "</html>" );
		html.close();

		return message.toString();
	}

	private void writeCookies( PrintWriter html, HttpServletRequest request ) {
		if ( request.getCookies() != null ) {
			// Write cookies
			html.append( "<h5>Cookies</h5>" );
			html.print( TABLE_START_TAG );
			for ( Cookie cookie : request.getCookies() ) {
				StringBuffer sbuf = new StringBuffer();
				if ( cookie.getDomain() != null ) {
					sbuf.append( cookie.getDomain() ).append( " " );
				}
				if ( cookie.getPath() != null ) {
					sbuf.append( cookie.getPath() ).append( " " );
				}
				sbuf.append( cookie.getMaxAge() ).append( "<br/>" ).append( cookie.getValue() );

				writeParam( html, cookie.getName(), sbuf.toString() );
			}
			html.append( TABLE_END_TAG );
		}
	}

	private void writeRequestAttributes( PrintWriter html, HttpServletRequest request ) {
		html.append( "<h5>Request attributes</h5>" );
		html.print( TABLE_START_TAG );
		Enumeration enumeration = request.getAttributeNames();
		while ( enumeration.hasMoreElements() ) {
			String attributeName = (String) enumeration.nextElement();

			writeParam( html, attributeName, request.getAttribute( attributeName ) );
		}
		html.append( TABLE_END_TAG );
	}

	private void writeSessionAttributes( PrintWriter html, HttpServletRequest request ) {
		html.append( "<h5>Session attributes</h5>" );
		html.print( TABLE_START_TAG );
		HttpSession session = request.getSession( false );
		if ( session != null ) {
			writeParam( html, "Session Id: ", session.getId() );
			writeParam( html, "Creation Time: ", session.getCreationTime() );
			writeParam( html, "Last Accessed Time: ", session.getLastAccessedTime() );
			writeParam( html, "Maximmum Inactive Interval: ", session.getMaxInactiveInterval() );
			writeParam( html, "New Session? ", session.isNew() );
			Enumeration enumeration = session.getAttributeNames();
			while ( enumeration.hasMoreElements() ) {
				String attributeName = (String) enumeration.nextElement();

				writeParam( html, attributeName, session.getAttribute( attributeName ) );
			}
		}
		else {
			writeParam( html, "No session", "" );
		}
		html.append( TABLE_END_TAG );
	}

	private void writeRequestHeaders( PrintWriter html, HttpServletRequest request ) {
		html.append( "<h5>Request headers</h5>" );
		html.print( TABLE_START_TAG );
		Enumeration enumeration = request.getHeaderNames();
		while ( enumeration.hasMoreElements() ) {
			String headerName = (String) enumeration.nextElement();

			if ( !StringUtils.equalsIgnoreCase( "cookie", headerName ) ) {
				writeParam( html, headerName, request.getHeader( headerName ) );
			}
		}
		html.append( TABLE_END_TAG );
	}

	private void writeRequestParameters( PrintWriter html, HttpServletRequest request ) {
		html.append( "<h5>Request parameters</h5>" );
		html.print( TABLE_START_TAG );
		Enumeration enumeration = request.getParameterNames();
		while ( enumeration.hasMoreElements() ) {
			String parameterName = (String) enumeration.nextElement();

			writeParam( html, parameterName, request.getParameter( parameterName ) );
		}
		html.append( TABLE_END_TAG );
	}

	private String getRequestDuration( HttpServletRequest request ) {
		Long startTime = (Long) request.getAttribute( RequestLogInterceptor.ATTRIBUTE_START_TIME );

		if ( startTime == null ) {

			return "unavailable";
		}
		else {

			long duration = System.currentTimeMillis() - startTime;
			return duration + " ms";
		}
	}

	private void writeParam( PrintWriter html, String paramName, Object paramValue ) {
		html.append( "<tr><td><strong>" ).append( paramName ).append( "</strong></td><td>" ).print( paramValue );
		html.append( "</td></tr>" );
	}

	private String createUrlFromRequest( HttpServletRequest request ) {
		StringBuffer buf = request.getRequestURL();
		String qs = request.getQueryString();

		if ( qs != null ) {
			buf.append( '?' ).append( qs );
		}

		return buf.toString();
	}

	private static class DefaultExceptionPredicate implements ExceptionPredicate
	{
		@Override
		public boolean evaluate( Exception exception ) {
			return true;
		}
	}
}
