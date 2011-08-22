package com.foreach.mail;

import com.foreach.context.WebApplicationContext;
import com.foreach.context.WebCacheBypassRequest;
import com.foreach.logging.RequestLogInterceptor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Date;
import java.util.Enumeration;

/**
 * <p>This class resolves every java exception occurred to a mail.</p>
 * To use this resolver, declare a bean for this class in your spring configuration file.
 * Usage in spring configuration file:
 * </p>
 *
 * <pre>
 *  &lt;bean class="com.foreach.mail.ExceptionToMailResolver"&gt;
 *		&lt;property name="fromAddress" value="mail from address"/&gt;
 *		&lt;property name="toAddress" value="mail to address"/&gt;
 *		&lt;property name="order" value="1"/&gt;
 *		&lt;property name="exceptionMappings"&gt;
 *			&lt;props&gt;
 *				&lt;prop key="java.lang.Throwable"&gt;error&lt;/prop&gt;
 *			&lt;/props&gt;
 *		&lt;/property&gt;
 *	    &lt;property name="mailService" ref="mail service bean name"/&gt;
 *	    &lt;property name="asyncService" ref="async service bean name"/&gt;
 *	    &lt;property name="applicationContext" ref="web application context bean name"/&gt;
 *	&lt;/bean&gt;
 * </pre>
 *
 * <p> When you create bean for this class, following properties are mandatory and its value need to be provided.</p>
 * <ul>
 *     <li>fromAddress</li>
 *     <li>toAddress</li>
 *     <li>order</li>
 *     <li>exceptionMappings</li>
 *     <li>mailService, provide the bean name of MailService class</li>
 *     <li>asyncService, provide the bean name of AsyncService class</li>
 *     <li>applicationContext, provide the bean name of WebApplicationContext class</li>
 * </ul>
 */
public class ExceptionToMailResolver extends SimpleMappingExceptionResolver
{
	private static Logger log = Logger.getLogger( ExceptionToMailResolver.class );

	private String fromAddress, toAddress;

    @Autowired
    public MailService mailService;

	@Autowired
	private WebApplicationContext applicationContext;

    public final void setLogger(Logger logger)
    {
        this.log = logger;
    }

	public final void setFromAddress( String fromAddress )
	{
		this.fromAddress = fromAddress;
	}

	public final void setToAddress( String toAddress )
	{
		this.toAddress = toAddress;
	}

    public final void setMailService( MailService mailService )
    {
        this.mailService = mailService;
    }

    public final void setWebApplicationContext( WebApplicationContext context )
    {
        this.applicationContext = context;
    }


	@Override
	protected final ModelAndView doResolveException(
			HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex )
	{
		log.error( "Exception has occured ", ex );

		try {
			if ( ex != null ) {
				String mailBody = createExceptionMailBody( request, handler, ex );
				String mailSubject = createExceptionMailSubject( ex );

                mailService.sendMimeMail(fromAddress, toAddress, null, mailSubject, mailBody, null);
			}
		}
		catch ( RuntimeException rex ) {
			log.error( "New exception when handling exception ", rex );
		}

		return super.doResolveException( request, response, handler, ex );
	}

	private String createExceptionMailSubject( Exception ex )
	{
		return new StringBuffer( "[" ).append( applicationContext.getLabel() ).append( "-" ).append(
				applicationContext.getApplicationName() ).append( " v" ).append(
				applicationContext.getBuildNumber() ).append( "] " ).append( ex.getClass().toString() ).toString();
	}

	private String createExceptionMailBody(
			HttpServletRequest request, Object handler, Exception ex )
	{
		DateFormat readableDate = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss" );

		Date now = new Date();

		StringWriter message = new StringWriter();
		PrintWriter html = new PrintWriter( message );

		// Write general params
		html.print( "<html><head></head><body style='font-family: tahoma;font-size: 12px;'>" );
		html.print( "<table border='1' cellpadding='3' style='font-family: tahoma;font-size: 12px;'>" );
		writeParam( html, "request id", request.getAttribute(
				RequestLogInterceptor.ATTRIBUTE_UNIQUE_ID ) + " (duration: " + getRequestDuration(
				request ) + ")" );
		writeParam( html, "date", readableDate.format( now ) );
		writeParam( html, "site",
		            applicationContext.getLabel() + "-" + applicationContext.getApplicationName() + " (" + applicationContext.getEnvironment() + ")" );
		writeParam( html, "build", "v" + applicationContext.getBuildNumber() + " (build date: " + readableDate.format(
				applicationContext.getBuildDate() ) + ")" );
		writeParam( html, "uptime",
		            DurationFormatUtils.formatDuration(now.getTime() - applicationContext.getStartupDate().getTime(),
                            "d'd' H'h' m'm'") + " (started: " + readableDate.format(
				            applicationContext.getStartupDate() ) + ")" );
		writeParam( html, "server", request.getServerName() );
		writeParam( html, "URL", request.getMethod() + " " + createUrlFromRequest( request ) );
		writeParam( html, "User-Agent", StringUtils.defaultIfBlank(request.getHeader("User-Agent"), "-") );
		writeParam( html, "Remote IP", StringUtils.defaultIfBlank( new WebCacheBypassRequest( request ).getRemoteAddr(), "-" ) );
		writeParam( html, "Referer", StringUtils.defaultIfBlank( request.getHeader( "Referrer" ), "-" ) );
		writeParam( html, "controller", handler != null ? handler.getClass() : "-" );
		writeParam( html, "view", StringUtils.defaultIfBlank(
				(String) request.getAttribute( RequestLogInterceptor.ATTRIBUTE_VIEW_NAME ), "-" ) );
		writeParam( html, "user", request.getUserPrincipal() != null ? StringUtils.defaultIfBlank(
				request.getUserPrincipal().getName(), "-" ) : "-" );
		html.append( "</table>" );

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

	private void writeCookies( PrintWriter html, HttpServletRequest request )
	{
		if ( request.getCookies() != null ) {
			// Write cookies
			html.append( "<h5>Cookies</h5>" );
			html.print( "<table border='1' cellpadding='3' style='font-family: tahoma;font-size: 12px;'>" );
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
			html.append( "</table>" );
		}
	}

	private void writeRequestAttributes( PrintWriter html, HttpServletRequest request )
	{
		html.append( "<h5>Request attributes</h5>" );
		html.print( "<table border='1' cellpadding='3' style='font-family: tahoma;font-size: 12px;'>" );
		Enumeration enumeration = request.getAttributeNames();
		while ( enumeration.hasMoreElements() ) {
			String attributeName = (String) enumeration.nextElement();

			writeParam( html, attributeName, request.getAttribute( attributeName ) );
		}
		html.append( "</table>" );
	}

	private void writeSessionAttributes( PrintWriter html, HttpServletRequest request )
	{
		html.append( "<h5>Session attributes</h5>" );
		html.print( "<table border='1' cellpadding='3' style='font-family: tahoma;font-size: 12px;'>" );
		HttpSession session = request.getSession( false );
		if( session != null ) {
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
		} else {
			writeParam( html, "No session", "" );
		}
		html.append( "</table>" );
	}

	private void writeRequestHeaders( PrintWriter html, HttpServletRequest request )
	{
		html.append( "<h5>Request headers</h5>" );
		html.print( "<table border='1' cellpadding='3' style='font-family: tahoma;font-size: 12px;'>" );
		Enumeration enumeration = request.getHeaderNames();
		while ( enumeration.hasMoreElements() ) {
			String headerName = (String) enumeration.nextElement();

			if ( !StringUtils.equalsIgnoreCase( "cookie", headerName ) ) {
				writeParam( html, headerName, request.getHeader( headerName ) );
			}
		}
		html.append( "</table>" );
	}

	private void writeRequestParameters( PrintWriter html, HttpServletRequest request )
	{
		html.append( "<h5>Request parameters</h5>" );
		html.print( "<table border='1' cellpadding='3' style='font-family: tahoma;font-size: 12px;'>" );
		Enumeration enumeration = request.getParameterNames();
		while ( enumeration.hasMoreElements() ) {
			String parameterName = (String) enumeration.nextElement();

			writeParam( html, parameterName, request.getParameter( parameterName ) );
		}
		html.append( "</table>" );
	}

	private String getRequestDuration( HttpServletRequest request )
	{
		long startTime = (Long) request.getAttribute( RequestLogInterceptor.ATTRIBUTE_START_TIME );
		long duration = System.currentTimeMillis() - startTime;

		return duration + " ms";
	}

	private void writeParam( PrintWriter html, String paramName, Object paramValue )
	{
		html.append( "<tr><td><strong>" ).append( paramName ).append( "</strong></td><td>" ).print( paramValue );
		html.append( "</td></tr>" );
	}

	private String createUrlFromRequest( HttpServletRequest request )
	{
		StringBuffer buf = request.getRequestURL();
		String qs = request.getQueryString();

		if ( qs != null ) {
			buf.append( '?' ).append( qs );
		}

		return buf.toString();
	}
}
