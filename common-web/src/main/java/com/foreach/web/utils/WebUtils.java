package com.foreach.web.utils;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Class contains utility methods for web related operations.
 */
public final class WebUtils
{
    private WebUtils()
    {
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
    public final static String getRemoteAddress(HttpServletRequest request)
    {
        String client = request.getParameter("clientip");
        if (!StringUtils.isBlank(client))
        {
            return client;
        }

        client = request.getHeader("CLIENTIP");
        if (!StringUtils.isBlank(client))
        {
            return client;
        }

        client = request.getHeader("X-Forwarded-For");
        if (!StringUtils.isBlank(client))
        {
            return client;
        }

        return request.getRemoteAddr();
    }

    /**
     * To get the port number of the server from which this request was invoked
     *
     * @param request
     * @return
     */
    public final static int getServerPort(HttpServletRequest request)
    {
        return request.getServerPort() == 7778 ? 80 : request.getServerPort();
    }
}
