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

import com.foreach.common.mybatis.enums.CodeBasedEnumHandler;
import com.foreach.common.spring.enums.EnumUtils;
import com.mockrunner.mock.jdbc.MockCallableStatement;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.MockResultSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class TestCountryHandler
{
	private class CountryHandler extends CodeBasedEnumHandler<Country>
	{
	}

	private final String[] countryCodes = new String[] { "Aus", "Zim", "ZIM", "Foo", null };

	private CountryHandler handler;

	@BeforeEach
	public void prepareForTest() {
		handler = new CountryHandler();
	}

	@Test
	public void codeToCountryConversion() throws SQLException {
		// Verify code to country conversion
		MockResultSet rs = new MockResultSet( "" );
		rs.addColumn( "country", countryCodes );

		while ( rs.next() ) {
			Country country = (Country) handler.getResult( rs, "country" );
			String code = (String) rs.getObject( "country" );

			assertSame( EnumUtils.getByCode( Country.class, code ), country );

			if ( country != null ) {
				assertTrue( country.getCode().equalsIgnoreCase( code ) );
			}
		}
	}

	@Test
	public void countryToCodeConversion() throws SQLException {
		// Verify country to code conversion
		MockPreparedStatement stmt = new MockPreparedStatement( new MockConnection(), "" );

		for ( String code : countryCodes ) {
			Country country = Country.getByCode( code );

			handler.setParameter( stmt, 1, country, JdbcType.VARCHAR );

			if ( country != null ) {
				assertTrue( StringUtils.equalsIgnoreCase( code, (String) stmt.getParameter( 1 ) ) );
			}
			else {
				assertNull( stmt.getParameter( 1 ) );
			}
		}
	}

	@Test()
	public void callableNotSupported() {
		MockCallableStatement stmt = new MockCallableStatement( new MockConnection(), "" );

		assertThrows(UnsupportedOperationException.class, ()->handler.getResult( stmt, 1 ));
	}

}
