package com.foreach.mybatis.util;

import org.apache.ibatis.type.JdbcType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class IntegerIdBasedEnumHandler<E extends Enum<E> & IntegerIdLookup>

		extends IdBasedEnumHandler<Integer,E>
{

	protected final void setParameter( PreparedStatement preparedStatement, int i, E e ) throws SQLException
	{
		if( e != null) {
			preparedStatement.setInt( i, e.getId() );
		} else {
			preparedStatement.setNull( i, JdbcType.INTEGER.TYPE_CODE );
		}
	}

	public final E getResult( ResultSet resultSet, String columnName ) throws SQLException
	{
		return getById( resultSet.getInt( columnName ) );
	}

}
