package com.foreach.mybatis.util;

import org.apache.commons.lang.NotImplementedException;
import org.apache.ibatis.type.JdbcType;

import javax.tools.JavaCompiler;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseEnumHandler<T, E extends Enum<E>>
{
	protected Class<E> clazz;

	protected E defaultValue;

	protected Integer customJdbcType;

	private Map<String,Class<?>> map = new HashMap<String, Class<?>>();


	protected BaseEnumHandler( Class<E> clazz )
	{
		this( clazz, null, null );
	}

	protected BaseEnumHandler( Class<E> clazz, E defaultValue )
	{
		this( clazz, defaultValue, null );
	}

	protected BaseEnumHandler( Class<E> clazz, E defaultValue, Integer customJdbcType )
	{
		this.clazz = clazz;
		this.defaultValue = defaultValue;
		this.customJdbcType = customJdbcType;

		// JDBC api designers were asleep at the wheel, it seems...
		if(customJdbcType != null) {
			map.put( JdbcUtils.getJdbcTypeName( customJdbcType ), clazz );
		}
	}

	protected final void setParameter( PreparedStatement preparedStatement, int i, T t ) throws SQLException
	{
		if( customJdbcType == null) {
			preparedStatement.setObject( i, t );
		} else {
			preparedStatement.setObject( i, t, customJdbcType );
		}
	}

	protected final T getParameter( ResultSet resultSet, String columnName ) throws SQLException
	{
		if( customJdbcType == null) {
			return (T) resultSet.getObject( columnName );
		} else {
			return (T) resultSet.getObject( columnName, map );
		}
	}

	public final Object getResult( CallableStatement callableStatement, int columnIndex ) throws SQLException
	{
		throw new NotImplementedException( getClass().getName()+" does not support CallableStatements" );
	}
}
