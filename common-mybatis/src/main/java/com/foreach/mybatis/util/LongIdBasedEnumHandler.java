package com.foreach.mybatis.util;

import com.foreach.utils.IdLookup;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class LongIdBasedEnumHandler <E extends Enum<E> & IdLookup<Long>>

		extends IdBasedEnumHandler<Long,E>
{

	protected final void setParameter( PreparedStatement preparedStatement, int i, Long parameter ) throws SQLException
	{
		preparedStatement.setLong( i, parameter );
	}

	protected final Long getParameter( ResultSet resultSet, String columnName ) throws SQLException
	{
		return resultSet.getLong( columnName );
	}

}
