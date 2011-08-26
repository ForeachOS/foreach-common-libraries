package com.foreach.test.web.logging;

import com.foreach.web.logging.RequestLogInterceptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class TestRequestLogInterceptor
{
	private RequestLogInterceptor interceptor;

	@Before
	public void prepareForTest()
	{
		interceptor = new RequestLogInterceptor();
	}

	@Test
	public void preHandlePostConditions()
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		interceptor.preHandle( request, response, null );

		Assert.assertNotNull( response.getHeader( RequestLogInterceptor.HEADER_REQUEST_ID ) );
		Assert.assertNotNull( request.getAttribute( RequestLogInterceptor.ATTRIBUTE_UNIQUE_ID ) );
		Assert.assertNotNull( request.getAttribute( RequestLogInterceptor.ATTRIBUTE_START_TIME ) );
	}
}
