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

import com.foreach.common.mybatis.enums.IdBasedEnumHandler;
import com.foreach.common.spring.enums.EnumUtils;
import com.mockrunner.mock.jdbc.MockCallableStatement;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.MockResultSet;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class TestCountryHandler3
{
	private class CountryHandler extends IdBasedEnumHandler<Country>
	{
		public CountryHandler() {
			super( null, JdbcType.DECIMAL );
		}
	}

	private final Long[] countryIds = new Long[] { 100000001L, 100000002L, 1L, null };

	private CountryHandler handler;

	@BeforeEach
	public void prepareForTest() {
		handler = new CountryHandler();
	}

	@Test
	public void idToCountryConversion() throws SQLException {
		// Verify code to country conversion
		MockResultSet rs = new MockResultSet( "" );
		rs.addColumn( "country", countryIds );

		while ( rs.next() ) {
			Country country = handler.getResult( rs, "country" );
			Long id = (Long) rs.getObject( "country" );

			assertSame( EnumUtils.getById( Country.class, id ), country );

			if ( country != null ) {
				assertTrue( country.getId().equals( id ) );
			}
		}
	}

	@Test
	public void countryToIdConversion() throws SQLException {
		// Verify country to code conversion
		MockPreparedStatement stmt = new MockPreparedStatement( new MockConnection(), "" );

		for ( Long id : countryIds ) {
			Country country = Country.getById( id );

			handler.setParameter( stmt, 1, country, JdbcType.DECIMAL );

			if ( country != null ) {
				assertEquals( id, (Long) stmt.getParameter( 1 ) );
			}
			else {
				assertNull( stmt.getParameter( 1 ) );
			}
		}
	}

	@Test
	public void callableNotSupported() {
		MockCallableStatement stmt = new MockCallableStatement( new MockConnection(), "" );

		assertThrows( UnsupportedOperationException.class,() ->handler.getResult( stmt, 1 ));
	}
}
