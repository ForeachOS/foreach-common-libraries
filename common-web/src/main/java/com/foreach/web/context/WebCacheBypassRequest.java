package com.foreach.web.context;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Utility class to retrieve the remote machine ip address and port from the given request object
 */
public class WebCacheBypassRequest extends HttpServletRequestWrapper
{
	public WebCacheBypassRequest( HttpServletRequest request )
	{
		super( request );
	}

	/**
	 * There are 4 ways the request could have reached the server :
	 * a) Straight from the client (no WebCache, no Rewrite with Proxy), happens on dev and on staging for some urls
	 * -> super.getRemoteAddr() gives correct result
	 * b) Proxy but not WebCache, happens on staging for some urls
	 * -> super.getHeader(X-Forwarded-For) gives correct result
	 * c) WebCache but not Proxy, happens on production servers for some urls
	 * -> super.getHeader(CLIENTIP) gives correct result
	 * d) WebCache, then Proxy, then WebCache, happens on production servers for some urls
	 * -> super.getParameter(clientip) gives correct result (rewrite rule puts contents of
	 * CLIENTIP header in querystring)
	 * These cases are handled in reverse order, each time passing through the next if the value they're
	 * trying to use turns out to be empty
	 *
	 * @return the original ip from which the request originated
	 */
	public final String getRemoteAddr()
	{
		String client = super.getParameter( "clientip" );
		if ( !StringUtils.isBlank(client) ) {
			return client;
		}

		client = super.getHeader( "CLIENTIP" );
		if ( !StringUtils.isBlank( client ) ) {
			return client;
		}

		client = super.getHeader( "X-Forwarded-For" );
		if ( !StringUtils.isBlank( client ) ) {
			return client;
		}

		return super.getRemoteAddr();
	}

	public final int getServerPort()
	{
		return super.getServerPort() == 7778 ? 80 : super.getServerPort();
	}
}
