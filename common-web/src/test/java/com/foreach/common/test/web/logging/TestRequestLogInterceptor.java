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
package com.foreach.common.test.web.logging;

import com.foreach.common.web.logging.RequestLogInterceptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class TestRequestLogInterceptor
{
	private RequestLogInterceptor interceptor;

	@Before
	public void prepareForTest() {
		interceptor = new RequestLogInterceptor();
	}

	@Test
	public void preHandlePostConditions() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		interceptor.preHandle( request, response, null );

		Assert.assertNotNull( response.getHeader( RequestLogInterceptor.HEADER_REQUEST_ID ) );
		Assert.assertNotNull( request.getAttribute( RequestLogInterceptor.ATTRIBUTE_UNIQUE_ID ) );
		Assert.assertNotNull( request.getAttribute( RequestLogInterceptor.ATTRIBUTE_START_TIME ) );
	}
}
