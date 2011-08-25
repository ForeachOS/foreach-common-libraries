package com.foreach.mybatis.util;

import org.apache.commons.lang.NotImplementedException;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.ParameterizedType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

abstract class BaseEnumHandler<E extends Enum<E>>
{
	private Class<E> clazz;

	private E defaultValue;

	private JdbcType customJdbcType;

	private Map<String, Class<?>> map = new HashMap<String, Class<?>>();

	protected BaseEnumHandler( E defaultValue, JdbcType customJdbcType )
	{
		this.clazz = getParameterClass();
		this.defaultValue = defaultValue;
		this.customJdbcType = customJdbcType;

		if ( customJdbcType != null ) {
			map.put( customJdbcType.name(), clazz );
		}
	}

	protected BaseEnumHandler( E defaultValue )
	{
		this( defaultValue, null );
	}

	protected BaseEnumHandler( Class<E> clazz )
	{
		this( null, null );
	}

	public final Class<E> getClazz()
	{
		return clazz;
	}

	public final E getDefaultValue()
	{
		return defaultValue;
	}

	public final JdbcType getCustomJdbcType()
	{
		return customJdbcType;
	}

	public final Object getResult( CallableStatement callableStatement, int columnIndex ) throws SQLException
	{
		throw new NotImplementedException( getClass().getName() + " does not support CallableStatements" );
	}

	protected final void setJdbcParameter(
			PreparedStatement preparedStatement, int i, Object t, JdbcType jdbcType ) throws SQLException
	{
		if ( customJdbcType == null ) {
			preparedStatement.setObject( i, t, jdbcType.TYPE_CODE );
		}
		else {
			preparedStatement.setObject( i, t, customJdbcType.TYPE_CODE );
		}
	}

	protected final Object getParameter( ResultSet resultSet, String columnName ) throws SQLException
	{
		if ( customJdbcType == null ) {
			return resultSet.getObject( columnName );
		}
		else {
			return resultSet.getObject( columnName, map );
		}
	}

	private Class getParameterClass()
	{
		ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();

		return (Class) pt.getActualTypeArguments()[0];
	}
}
