package com.foreach.mybatis.util;

import com.foreach.utils.CodeLookup;
import com.foreach.utils.EnumUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class CodeBasedEnumHandler<S,E extends Enum<E> & CodeLookup<S>>

		extends AbstractEnumHandler<E>

		implements TypeHandler
{
	private E getByCode( S code )
	{
		E e = EnumUtils.getByCode( getClazz(), code );
		return ( e == null ) ? getDefaultValue() : e;
	}

	public final void setParameter(
			PreparedStatement preparedStatement, int i, Object parameter, JdbcType jdbcType ) throws SQLException
	{
		CodeLookup<S> e = ( CodeLookup<S> ) parameter;

		setParameter( preparedStatement, i, (e != null)? e.getCode() : null ) ;
	}

	public final Object getResult( ResultSet resultSet, String columnName ) throws SQLException
	{
		return getByCode( getParameter( resultSet, columnName ) );
	}

	protected abstract void setParameter( PreparedStatement preparedStatement, int i, S s ) throws SQLException;

	protected abstract S getParameter( ResultSet resultSet, String columnName ) throws SQLException;
}
