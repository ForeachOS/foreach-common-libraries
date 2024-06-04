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

import com.foreach.common.spring.enums.CodeLookup;
import com.foreach.common.spring.enums.IdLookup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDualInterface extends BaseConversionServiceTest
{
	@Test
	public void useCodeLookup() {
		assertEquals( Foo.FOO, conversionService.convert( "foo", Foo.class ) );
		assertEquals( Foo.BOZ, conversionService.convert( "7", Foo.class ) );
	}

	public enum Foo implements CodeLookup<String>, IdLookup<Integer>
	{
		FOO( 1, "foo" ),
		BAR( 3, "bar" ),
		BOZ( 7, "boz" );

		private int id;
		private String code;

		Foo( int id, String code ) {
			this.id = id;
			this.code = code;
		}

		public Integer getId() {
			return id;
		}

		public String getCode() {
			return code;
		}
	}
}
