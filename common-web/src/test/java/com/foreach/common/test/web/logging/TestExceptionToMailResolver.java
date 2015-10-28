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

import com.foreach.common.spring.context.ApplicationContextInfo;
import com.foreach.common.spring.context.ApplicationEnvironment;
import com.foreach.common.spring.mail.MailService;
import com.foreach.common.web.logging.ExceptionPredicate;
import com.foreach.common.web.logging.ExceptionToMailResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Date;
import java.util.Map;

import static org.mockito.Mockito.*;

public class TestExceptionToMailResolver
{

	private ExceptionToMailResolver resolver;

	private MailService mailService;
	private ApplicationContextInfo applicationContextInfo;
	private String toAddress;
	private String fromAddress;

	@Before
	public void prepareForTest() {
		resolver = new ExceptionToMailResolver();

		mailService = mock( MailService.class );

		applicationContextInfo = new ApplicationContextInfo();
		applicationContextInfo.setApplicationName( "TestExceptionToMailResolver" );
		applicationContextInfo.setBuildNumber( 1L );
		applicationContextInfo.setEnvironment( ApplicationEnvironment.TEST );
		applicationContextInfo.setBuildDate( new Date() );

		toAddress = "devnull@foreach.be";
		fromAddress = "noreply@foreach.be";

		resolver.setMailService( mailService );
		resolver.setApplicationContext( applicationContextInfo );
		resolver.setToAddress( toAddress );
		resolver.setFromAddress( fromAddress );
	}

	@Test
	// Don't break if request attributes set by an optional RequestlogInterceptor are unavailable when throwing an Exception.
	public void optionalRequestLogInterceptor() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		resolver.doResolveException( request, response, null, new Exception() );

		verify( mailService ).sendMimeMail( eq( fromAddress ), eq( toAddress ), anyString(), anyString(), anyString(),
		                                    Matchers.<Map<String, File>>anyObject() );
	}

	@Test
	public void resolverRecoversIfMailServiceThrowsRuntimeException() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		when( mailService.sendMimeMail( anyString(), anyString(), anyString(), anyString(), anyString(),
		                                Matchers.<Map<String, File>>anyObject() ) ).thenThrow(
				new NullPointerException() );

		resolver.doResolveException( request, response, null, new Exception() );
	}

	@Test
	public void addCookiesAndAttributesAndStuffLikeForARealMessage() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		Cookie lu = new Cookie( "lu", "bah" );
		lu.setDomain( "www.cookie.org" );
		lu.setPath( "/blikkendoos/" );

		Cookie delacre = new Cookie( "delacre", "gaatwel" );
		Cookie destrooper = new Cookie( "destrooper", "toch wat vettig" );

		request.setCookies( lu, delacre, destrooper );

		request.setAttribute( "casting cost", "2 witte bollekes" );

		request.addParameter( "kleur", "donkere oker" );

		request.addHeader( "X-Dontlookatthecontent", "if so inclined" );

		HttpSession session = new MockHttpSession();
		session.setAttribute( "sessionId", "supposedToBeALongString" );

		request.setSession( session );

		resolver.doResolveException( request, response, null, new Exception() );

		verify( mailService ).sendMimeMail( eq( fromAddress ), eq( toAddress ), anyString(), anyString(), anyString(),
		                                    Matchers.<Map<String, File>>anyObject() );
	}

	@Test
	public void resolverDoesNotSendMail()
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		resolver.setExceptionPredicate( new ExceptionPredicate()
		{
			@Override
			public boolean evaluate( Exception exception ) {
				return !(exception instanceof RuntimeException);
			}
		} );

		resolver.doResolveException( request, response, null, new RuntimeException() );

		verify( mailService, never() ).sendMimeMail( anyString(), anyString(), anyString(), anyString(), anyString(),
		                                             Matchers.<Map<String, File>>anyObject() );
	}

	@Test
	public void resolverDoesSendMail()
	{
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		resolver.setExceptionPredicate( new ExceptionPredicate()
		{
			@Override
			public boolean evaluate( Exception exception ) {
				return true;
			}
		} );
		resolver.doResolveException( request, response, null, new Exception() );

		verify( mailService ).sendMimeMail( anyString(), anyString(), anyString(), anyString(), anyString(),
		                                    Matchers.<Map<String, File>>anyObject() );
	}

}
