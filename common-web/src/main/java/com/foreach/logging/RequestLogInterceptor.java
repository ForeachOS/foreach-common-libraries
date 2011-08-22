package com.foreach.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicLong;


/**
 * <p>The RequestLogInterceptor class can be used to log Http request with execution time.</p>
 *
 * <p>Usage in spring configuration file:</p>
 * <pre>
 *  &lt;mvc:interceptors&gt;
 *	    &lt;bean class="com.foreach.logging.RequestLogInterceptor"/&gt;
 *  &lt;/mvc:interceptors&gt;
 * </pre>
 *
 * <p>You need to define a logger with appender in your log4j.properties file. See below logger definition</p>
 * <pre>
 *
 *  Logger definition
 *  log4j.logger.com.foreach.logging.RequestLogInterceptor=DEBUG,request
 *  log4j.additivity.com.foreach.logging.RequestLogInterceptor=false
 *
 *  Appender definition
 *  log4j.appender.request=org.apache.log4j.rolling.RollingFileAppender
 *  log4j.appender.request.rollingPolicy=org.apache.log4j.rolling.TimeBasedRollingPolicy
 *  log4j.appender.request.rollingPolicy.FileNamePattern=${log.dir}/%d{yyyy'/'MM'/'dd'/request.'yyyy-MM-dd}.log
 *  log4j.appender.request.layout=org.apache.log4j.PatternLayout
 *  log4j.appender.request.layout.ConversionPattern=%d{ISO8601} [%t] %m%n
 * </pre>
 *
 * <p><em>Log interceptor should be declared as first bean in mvc:interceptors (if there are many interceptors),
 * so that we can log the timings of all other interceptors.</em></p>
 *
 * @version 1.0
 */
public class RequestLogInterceptor implements HandlerInterceptor
{
	public static final String ATTRIBUTE_START_TIME = "_log_requestStartTime";
	public static final String ATTRIBUTE_UNIQUE_ID = "_log_uniqueRequestId";
	public static final String ATTRIBUTE_VIEW_NAME = "_log_resolvedViewName";
	public static final String HEADER_REQUEST_ID = "Request-Reference";

	private static final Logger LOG = Logger.getLogger( RequestLogInterceptor.class );

	private final AtomicLong counter = new AtomicLong( System.currentTimeMillis() );

	public final boolean preHandle(
			HttpServletRequest request, HttpServletResponse response, Object handler ) throws Exception
	{
		// Create a unique id for this request
		String requestId = "" + counter.getAndIncrement();

		// Put id as NDC for log4j: this can be used to identify this request in the log files
		NDC.push(requestId);

		response.setHeader( HEADER_REQUEST_ID, requestId );
		request.setAttribute( ATTRIBUTE_UNIQUE_ID, requestId );
		request.setAttribute( ATTRIBUTE_START_TIME, System.currentTimeMillis() );

		return true;
	}

	public final void postHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler,
			ModelAndView modelAndView ) throws Exception
	{
		// Redirects won't have a modelAndView
		if ( modelAndView != null ) {
			request.setAttribute( ATTRIBUTE_VIEW_NAME, modelAndView.getViewName() );
		}
	}

	public final void afterCompletion(
			HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex ) throws Exception
	{
		// Determine duration before sending the log
		long startTime = (Long) request.getAttribute( ATTRIBUTE_START_TIME );
		long duration = System.currentTimeMillis() - startTime;

		String handlerName = handler != null ? handler.getClass().toString() : "no-handler";

		StringBuffer buf = new StringBuffer();
		buf.append( '[' ).append( request.getAttribute( ATTRIBUTE_UNIQUE_ID ) ).append( ']' ).append( '\t' ).append(
				request.getRemoteAddr() ).append( '\t' ).append( request.getMethod() ).append( '\t' ).append(
				createUrlFromRequest( request ) ).append( '\t' ).append( request.getServletPath() ).append(
				'\t' ).append( handlerName ).append( '\t' ).append(
				request.getAttribute( ATTRIBUTE_VIEW_NAME ) ).append( '\t' ).append( duration );

		LOG.debug( buf.toString() );

		// Remove the NDC
		NDC.pop();
		NDC.remove();
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
