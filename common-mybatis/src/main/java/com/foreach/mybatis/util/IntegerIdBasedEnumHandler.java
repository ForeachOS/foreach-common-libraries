package com.foreach.mybatis.util;

import com.foreach.utils.IdLookup;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class IntegerIdBasedEnumHandler<E extends Enum<E> & IdLookup<Integer>>

		extends IdBasedEnumHandler<Integer,E>
{

	protected final void setParameter( PreparedStatement preparedStatement, int i, Integer parameter ) throws SQLException
	{
		preparedStatement.setInt( i, parameter );
	}

	protected final Integer getParameter( ResultSet resultSet, String columnName ) throws SQLException
	{
		return resultSet.getInt( columnName );
	}
}
