package com.foreach.mybatis.util;

import com.foreach.utils.CodeLookup;
import com.foreach.utils.EnumUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public abstract class StringCodeBasedEnumHandler<E extends Enum<E> & CodeLookup<String>>
		extends CodeBasedEnumHandler<String,E>
{

	protected void setParameter( PreparedStatement preparedStatement, int i, String s ) throws SQLException
	{
		preparedStatement.setString( i, s );
	}

	protected String getParameter( ResultSet resultSet, String columnName ) throws SQLException
	{
		return resultSet.getString( columnName );
	}

}
