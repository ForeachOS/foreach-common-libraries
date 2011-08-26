package com.foreach.test.mybatis.util;

import com.foreach.mybatis.util.CodeBasedEnumHandler;
import com.mockrunner.mock.jdbc.MockCallableStatement;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.MockResultSet;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public class TestCountryHandler
{
	private class CountryHandler extends CodeBasedEnumHandler<Country>
	{

	}

	private final String[] countryCodes = new String[] { "Aus", "Zim", "Foo", null };

	private CountryHandler handler;

	@Before
	public void prepareForTest()
	{
		handler = new CountryHandler();
	}

	@Test
	public void codeToCountryConversion() throws SQLException
	{
		// Verify code to country conversion
		MockResultSet rs = new MockResultSet( "" );
		rs.addColumn( "country", countryCodes );

		while ( rs.next() ) {
			Country country = (Country) handler.getResult( rs, "country" );
			String code = (String) rs.getObject( "country" );

			Assert.assertSame( Country.getByCode( code ), country );

			if ( country != null ) {
				Assert.assertTrue( country.getCode().equalsIgnoreCase( code ) );
			}
		}
	}

	@Test
	public void countryToCodeConversion() throws SQLException
	{
		// Verify country to code conversion
		MockPreparedStatement stmt = new MockPreparedStatement( new MockConnection(), "" );

		for ( String code : countryCodes ) {
			Country country = Country.getByCode( code );

			handler.setParameter( stmt, 1, country, JdbcType.VARCHAR );

			if ( country != null ) {
				Assert.assertTrue( StringUtils.equalsIgnoreCase( code, (String) stmt.getParameter( 1 ) ) );
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

		handler.getResult( stmt, 1 );
	}

}
