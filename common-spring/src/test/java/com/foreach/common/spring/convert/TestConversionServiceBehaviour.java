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
package com.foreach.common.spring.convert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 */
@SuppressWarnings("unchecked")
public class TestConversionServiceBehaviour
{
	private final User userOne = new User( "userOne" );
	private final User userTwo = new User( "userTwo" );

	private ConfigurableConversionService conversionService;

	@Before
	public void createConversionService() {
		conversionService = new DefaultConversionService();
		Converter<String, User> userConverter = mock( Converter.class );

		conversionService.addConverter( String.class, User.class, userConverter );

		when( userConverter.convert( "1" ) ).thenReturn( userOne );
		when( userConverter.convert( "2" ) ).thenReturn( userTwo );

		conversionService.addConverter( User.class, String.class, new Converter<User, String>()
		{
			@Override
			public String convert( User source ) {
				return source.getName();
			}
		} );
	}

	@Test
	public void convertStringArrayToListOfUsers() {
		String[] ids = new String[] { "1", "2" };

		List<User> userList = (List<User>) conversionService.convert( ids,
		                                                              TypeDescriptor.forObject( ids ),
		                                                              TypeDescriptor.collection( List.class,
		                                                                                         TypeDescriptor.valueOf(
				                                                                                         User.class ) ) );
		assertEquals( 2, userList.size() );
		assertTrue( userList.containsAll( Arrays.asList( userOne, userTwo ) ) );
	}

	@Test
	public void convertStringListToSetOfUsers() {
		List<String> idList = Arrays.asList( "1", "2" );

		Set<User> userSet = (Set<User>) conversionService.convert( idList,
		                                                           TypeDescriptor.forObject( idList ),
		                                                           TypeDescriptor.collection( LinkedHashSet.class,
		                                                                                      TypeDescriptor.valueOf(
				                                                                                      User.class ) ) );
		assertEquals( 2, userSet.size() );
		assertTrue( userSet.containsAll( Arrays.asList( userOne, userTwo ) ) );
	}

	@Test
	public void convertListOfUsersToListOfStrings() {
		List<User> users = Arrays.asList( userOne, userTwo );
		List<String> names = (List<String>) conversionService.convert( users,
		                                                               TypeDescriptor.forObject( users ),
		                                                               TypeDescriptor.collection( List.class,
		                                                                                          TypeDescriptor.valueOf(
				                                                                                          String.class ) ) );

		assertEquals( 2, names.size() );
		assertTrue( names.containsAll( Arrays.asList( "userOne", "userTwo" ) ) );
	}

	@Test
	public void convertListOfUsersToSingleString() {
		List<User> users = Arrays.asList( userOne, userTwo );
		String names = conversionService.convert( users, String.class );

		assertEquals( "userOne,userTwo", names );
	}

	@Test
	public void convertSingleStringToListOfUsers() {
		List<User> users = (List<User>) conversionService.convert( "1,2", TypeDescriptor.valueOf( String.class ),
		                                                           TypeDescriptor.collection( List.class,
		                                                                                      TypeDescriptor.valueOf(
				                                                                                      User.class ) ) );

		assertEquals( 2, users.size() );
		assertTrue( users.containsAll( Arrays.asList( userOne, userTwo ) ) );
	}

	@Test
	public void useStaticFromMethodConversion() {
		User user = new User( "someUser" );
		UserDto userDto = conversionService.convert( user, UserDto.class );

		assertNotNull( userDto );
		assertEquals( "someUser", userDto.getName() );
	}

	private static class User
	{
		private String name;

		private User( String name ) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public boolean equals( Object o ) {
			return this == o || o instanceof User && Objects.equals( name, ( (User) o ).name );
		}

		@Override
		public int hashCode() {
			return Objects.hash( name );
		}
	}

	private static class UserDto
	{
		private String name;

		private UserDto( String name ) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static UserDto from( User user ) {
			return new UserDto( user.getName() );
		}
	}
}
