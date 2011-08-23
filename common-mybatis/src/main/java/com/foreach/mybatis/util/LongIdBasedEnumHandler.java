package com.foreach.mybatis.util;

import org.apache.ibatis.type.JdbcType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class LongIdBasedEnumHandler <E extends Enum<E> & LongIdLookup>

		extends IdBasedEnumHandler<Long,E>
{

	protected void setParameter( PreparedStatement preparedStatement, int i, E e ) throws SQLException
	{
		if( e != null) {
			preparedStatement.setLong( i, e.getId() );
		} else {
			preparedStatement.setNull( i, JdbcType.INTEGER.TYPE_CODE );
		}
	}

	public final E getResult( ResultSet resultSet, String columnName ) throws SQLException
	{
		return getById( resultSet.getLong( columnName ) );
	}

}
