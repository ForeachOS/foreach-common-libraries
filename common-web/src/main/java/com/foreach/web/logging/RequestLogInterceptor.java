package com.foreach.web.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>RequestLogInterceptor times http requests and logs the result.</p>
 * <p/>
 * <p>Usage in spring configuration file:</p>
 * <pre>
 *  &lt;mvc:interceptors&gt;
 * 	    &lt;bean class="com.foreach.web.logging.RequestLogInterceptor"/&gt;
 *  &lt;/mvc:interceptors&gt;
 * </pre>
 * <p/>
 * <p>You need to define a logger with appender in your log4j.properties file or set your own custom logger using method setLogger.
 * See below for defining new logger in your log4j properties file</p>
 * <pre>
 *
 *  Logger definition
 *  log4j.logger.com.foreach.web.logging.RequestLogInterceptor=DEBUG,request
 *  log4j.additivity.com.foreach.web.logging.RequestLogInterceptor=false
 *
 *  Appender definition
 *  log4j.appender.request=org.apache.log4j.rolling.RollingFileAppender
 *  log4j.appender.request.rollingPolicy=org.apache.log4j.rolling.TimeBasedRollingPolicy
 *  log4j.appender.request.rollingPolicy.FileNamePattern=${log.dir}/%d{yyyy'/'MM'/'dd'/request.'yyyy-MM-dd}.log
 *  log4j.appender.request.layout=org.apache.log4j.PatternLayout
 *  log4j.appender.request.layout.ConversionPattern=%d{ISO8601} [%t] %m%n
 * </pre>
 * <p/>
 * <p><em>Ideally, a log interceptor should be declared as the first (outermost) interceptor in an interceptor chain
 * so the timing is as accurately as possible.</em></p>
 * <p/>
 * <p>
 * RequestLogInterceptor uses {@link org.apache.log4j.NDC org.apache.log4j.NDC} for storing a unique request id which can be referred back in Firebug.
 * Look for the request header attribute "Request-Reference" to know the generated unique request id.
 * </p>
 *
 * @version 1.0
 */
public class RequestLogInterceptor implements HandlerInterceptor
{
	public static final String ATTRIBUTE_START_TIME = "_log_requestStartTime";
	public static final String ATTRIBUTE_UNIQUE_ID = "_log_uniqueRequestId";
	public static final String ATTRIBUTE_VIEW_NAME = "_log_resolvedViewName";
	public static final String HEADER_REQUEST_ID = "Request-Reference";

	private final AtomicLong counter = new AtomicLong( System.currentTimeMillis() );

	private Logger logger = Logger.getLogger( getClass() );

	/**
	 * Specify your custom logger
	 *
	 * @param log
	 */
	protected final void setLogger( Logger log )
	{
		this.logger = log;
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
	 * {@inheritDoc}
	 */
	public final boolean preHandle(
			HttpServletRequest request, HttpServletResponse response, Object handler )
	{
		// Create a unique id for this request
		String requestId = "" + counter.getAndIncrement();

		// Put id as NDC for log4j: this can be used to identify this request in the log files
		NDC.push( requestId );

		response.setHeader( HEADER_REQUEST_ID, requestId );
		request.setAttribute( ATTRIBUTE_UNIQUE_ID, requestId );
		request.setAttribute( ATTRIBUTE_START_TIME, System.currentTimeMillis() );

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void postHandle(
			HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView )
	{
		// Redirects won't have a modelAndView
		if ( modelAndView != null ) {
			request.setAttribute( ATTRIBUTE_VIEW_NAME, modelAndView.getViewName() );
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void afterCompletion(
			HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex )
	{
		// Determine duration before sending the log
		long startTime = (Long) request.getAttribute( ATTRIBUTE_START_TIME );
		long duration = System.currentTimeMillis() - startTime;

		String handlerName = handler != null ? handler.getClass().toString() : "no-handler";

		StringBuilder buf = new StringBuilder();
		buf.append( '[' ).append( request.getAttribute( ATTRIBUTE_UNIQUE_ID ) ).append( ']' ).append( '\t' ).append(
				request.getRemoteAddr() ).append( '\t' ).append( request.getMethod() ).append( '\t' ).append(
				createUrlFromRequest( request ) ).append( '\t' ).append( request.getServletPath() ).append(
				'\t' ).append( handlerName ).append( '\t' ).append(
				request.getAttribute( ATTRIBUTE_VIEW_NAME ) ).append( '\t' ).append( duration );

		logger.debug( buf.toString() );

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
