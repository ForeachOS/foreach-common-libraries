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
package com.foreach.common.test.web.mapper;

import com.foreach.common.web.mapper.SubClassAllowingRequestHandlerMapping;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerExecutionChain;

import javax.management.AttributeList;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertSame;

public class TestSubClassAllowingRequestHandlerMapping
{
	private Mapper mapper;

	private MockHttpServletRequest request = new MockHttpServletRequest();

	@Before
	public void setup() {
		mapper = new Mapper();
	}

	@Test
	public void testRegisterHandler_simpleCase() throws Exception {
		Object handler = new Object();
		String url = "/simple";
		mapper.registerHandler( url, handler );

		HandlerExecutionChain actual = (HandlerExecutionChain) mapper.lookupHandler( url, request );
		assertSame( handler, actual.getHandler() );
	}

	@Test
	public void testRegisterHandler_subClassFirst() throws Exception {
		List handler1 = new ArrayList();
		List handler2 = new AttributeList();

		String url = "/simple";
		mapper.registerHandler( url, handler2 );
		mapper.registerHandler( url, handler1 );

		HandlerExecutionChain actual = (HandlerExecutionChain) mapper.lookupHandler( url, request );
		assertSame( handler2, actual.getHandler() );
	}

	@Test
	public void testRegisterHandler_subClassSecond() throws Exception {
		List handler1 = new ArrayList();
		List handler2 = new AttributeList();

		String url = "/simple";
		mapper.registerHandler( url, handler1 );
		mapper.registerHandler( url, handler2 );

		HandlerExecutionChain actual = (HandlerExecutionChain) mapper.lookupHandler( url, request );
		assertSame( handler2, actual.getHandler() );
	}

	@Test
	public void testRegisterHandler_rootPath_SubClassFirst() throws Exception {
		List handler1 = new ArrayList();
		List handler2 = new AttributeList();

		String url = "/";
		mapper.registerHandler( url, handler2 );
		mapper.registerHandler( url, handler1 );

		assertSame( handler2, mapper.getRootHandler() );
	}

	@Test
	public void testRegisterHandler_rootPath_simpleSubClassSecond() throws Exception {
		List handler1 = new ArrayList();
		List handler2 = new AttributeList();

		String url = "/";
		mapper.registerHandler( url, handler1 );
		mapper.registerHandler( url, handler2 );

		assertSame( handler2, mapper.getRootHandler() );
	}

	@Test(expected = IllegalStateException.class)
	public void testRegisterHandler_noSubClass() throws Exception {
		List handler1 = new ArrayList();
		Map handler2 = new HashMap();
		String url = "/simple";
		mapper.registerHandler( url, handler1 );
		mapper.registerHandler( url, handler2 );
	}

	@Test(expected = IllegalStateException.class)
	public void testRegisterHandler_rootPathNoSubClass() throws Exception {
		List handler1 = new ArrayList();
		HashMap handler2 = new HashMap();
		String url = "/";
		mapper.registerHandler( url, handler1 );
		mapper.registerHandler( url, handler2 );
	}

	private class Mapper extends SubClassAllowingRequestHandlerMapping
	{
		public void registerHandler( String urlPath, Object handler ) throws BeansException, IllegalStateException {
			super.registerHandler( urlPath, handler );
		}

		public Object lookupHandler( String urlPath, HttpServletRequest request ) throws Exception {
			return super.lookupHandler( urlPath, request );
		}
	}
}
