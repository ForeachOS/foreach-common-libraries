package com.foreach.mybatis.util;

import org.apache.commons.lang.NotImplementedException;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public abstract class CodeBasedEnumHandler <E extends Enum<E> & CodeLookup> implements TypeHandler
{
	protected abstract Class<E> getClazz();

	protected abstract E getDefaultValue();

	private E getByCode( String code )
	{
		E e = EnumUtils.getByCode( getClazz(), code );
		return ( e == null ) ? getDefaultValue() : e;
	}

	public final void setParameter(
			PreparedStatement preparedStatement, int i, Object parameter, JdbcType jdbcType ) throws SQLException
	{
		CodeLookup e = ( CodeLookup ) parameter;

		if( e != null ) {
			preparedStatement.setString( i, e.getCode() );
		} else {
			preparedStatement.setString( i, null ); // target column may be char[n] or varchar
		}
	}

	public final Object getResult( ResultSet resultSet, String columnName ) throws SQLException
	{
		return getByCode( resultSet.getString( columnName ) );
	}

	public final Object getResult( CallableStatement callableStatement, int columnIndex ) throws SQLException
	{
		throw new NotImplementedException( getClass().getName()+" does not support CallableStatements" );
	}
}
