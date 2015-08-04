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
package com.foreach.common.test.mybatis.util;

import com.foreach.common.spring.enums.CodeLookup;
import com.foreach.common.spring.enums.IdLookup;

public enum Country implements CodeLookup<String>, IdLookup<Long>
{
	AU( "aus", 100000001L ),
	ZB( "zim", 100000002L );

	private String code;
	private long largeNumber;

	Country( String code, long largeNumber ) {
		this.code = code;
		this.largeNumber = largeNumber;
	}

	public String getCode() {
		return code;
	}

	public Long getId() {
		return largeNumber;
	}

	public static final Country getByCode( String code ) {
		for ( Country c : values() ) {
			if ( c.getCode().equals( code ) ) {
				return c;
			}
		}

		return null;
	}

	public static final Country getById( Long id ) {
		for ( Country c : values() ) {
			if ( c.getId().equals( id ) ) {
				return c;
			}
		}

		return null;
	}
}
