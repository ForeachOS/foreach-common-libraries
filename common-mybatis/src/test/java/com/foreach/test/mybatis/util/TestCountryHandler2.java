package com.foreach.test.mybatis.util;

import com.foreach.mybatis.enums.IdBasedEnumHandler;
import com.mockrunner.mock.jdbc.MockCallableStatement;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.MockResultSet;
import org.apache.commons.lang.NotImplementedException;
import org.apache.ibatis.type.JdbcType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public class TestCountryHandler2
{
	private class CountryHandler extends IdBasedEnumHandler<Country>
	{

	}

	private final Long[] countryIds = new Long[] { 100000001L, 100000002L, 1L, null };

	private CountryHandler handler;

	@Before
	public void prepareForTest()
	{
		handler = new CountryHandler();
	}

	@Test
	public void idToCountryConversion() throws SQLException
	{
		// Verify code to country conversion
		MockResultSet rs = new MockResultSet( "" );
		rs.addColumn( "country", countryIds );

		while ( rs.next() ) {
			Country country = (Country) handler.getResult( rs, "country" );
			Long id = (Long) rs.getObject( "country" );

			Assert.assertSame( Country.getById( id ), country );

			if ( country != null ) {
				Assert.assertTrue( country.getId().equals( id ) );
			}
		}
	}

	@Test
	public void countryToIdConversion() throws SQLException
	{
		// Verify country to code conversion
		MockPreparedStatement stmt = new MockPreparedStatement( new MockConnection(), "" );

		for ( Long id : countryIds ) {
			Country country = Country.getById( id );

			handler.setParameter( stmt, 1, country, JdbcType.INTEGER );

			if ( country != null ) {
				Assert.assertTrue( id.equals( (Long) stmt.getParameter( 1 ) ) );
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
