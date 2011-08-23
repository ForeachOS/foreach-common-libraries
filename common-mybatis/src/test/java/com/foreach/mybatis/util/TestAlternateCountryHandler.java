package com.foreach.mybatis.util;

import com.mockrunner.mock.jdbc.MockCallableStatement;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.MockResultSet;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

public class TestAlternateCountryHandler
{
	private final Long[] countryIds = new Long[] { 100000001L, 100000002L, 1L, null };


	@Test
	public void codeToCountryConversion() throws SQLException
	{
		AlternateCountryHandler handler = new AlternateCountryHandler();

		// Verify code to country conversion
		MockResultSet rs = new MockResultSet( "" );
		rs.addColumn( "country", countryIds );

		while ( rs.next() ) {
			Country country = (Country) handler.getResult( rs, "country" );
			String code = rs.getString( "country" );

			Assert.assertSame( Country.getByCode( code ), country );

			if ( country != null ) {
				Assert.assertTrue( country.getCode().equals( code ) );
			}
		}
	}

	@Test
	public void countryToCodeConversion() throws SQLException
	{
		AlternateCountryHandler handler = new AlternateCountryHandler();

		// Verify country to code conversion
		MockPreparedStatement stmt = new MockPreparedStatement( new MockConnection(), "" );

		for ( Long id : countryIds ) {
			Country country = Country.getById( id );

			handler.setParameter( stmt, 1, country, JdbcType.INTEGER );

			if ( country != null ) {
				Assert.assertTrue( id == (Long) stmt.getParameter( 1 ) );
			}
			else {
				Assert.assertNull( stmt.getParameter( 1 ) );
			}
		}
	}

	@Test(expected = NotImplementedException.class)
	public void callableNotSupported() throws SQLException
	{
		MockCallableStatement stmt = new MockCallableStatement( new MockConnection(), "" );

		new CountryHandler().getResult( stmt, 1 );
	}

}
