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
package com.foreach.common.web.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Class contains utility methods for web related operations.
 */
public class WebUtils
{
	protected WebUtils() {
		// protected constructor, so that this class can be extended.
	}

	/**
	 * Retrieves the original ip address from which the request originated even if proxy-servers or web-caches are used.
	 *
	 * @param request the http request object
	 * @return the original ip from which the request originated
	 */
	public static String getRemoteAddress( HttpServletRequest request ) {
		// You can get the original ip address from which the request originated in following ways.
		// <ul>
		// <li>Straight from the client (no WebCache, no Rewrite with Proxy), happens on dev and on staging for some urls HttpServletRequest.getParameter("clientip") gives correct result</li>
		// <li>Proxy but not WebCache, happens on staging for some urls, HttpServletRequest.getHeader(X-Forwarded-For) gives correct result</li>
		// <li>WebCache but not Proxy, happens on production servers for some urls, HttpServletRequest.getHeader("CLIENTIP") gives correct result</li>
		// <li>WebCache, then Proxy, then WebCache, happens on production servers for some urls, HttpServletRequest.getParameter("clientip") gives correct result (rewrite rule puts contents of
		// CLIENTIP header in querystring).</li>
		// </ul>
		// <p/>
		// Above cases are handled in reverse order, each time passing through the next if the value they're
		// trying to use turns out to be empty

		String client = request.getParameter( "clientip" );
		if ( !StringUtils.isBlank( client ) ) {
			return client;
		}

		client = request.getHeader( "CLIENTIP" );
		if ( !StringUtils.isBlank( client ) ) {
			return client;
		}

		client = request.getHeader( "X-Forwarded-For" );
		if ( !StringUtils.isBlank( client ) ) {
			return client;
		}

		return request.getRemoteAddr();
	}

	/**
	 * To get the port number of the server from which this request was originated
	 *
	 * @param request the http request object
	 * @return port number of the server from which this request was originated
	 */
	public static int getServerPort( HttpServletRequest request ) {
		return request.getServerPort() == 7778 ? 80 : request.getServerPort();
	}
}
