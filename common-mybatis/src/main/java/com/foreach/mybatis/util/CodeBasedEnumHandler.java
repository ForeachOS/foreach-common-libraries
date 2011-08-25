package com.foreach.mybatis.util;

import com.foreach.utils.CodeLookup;
import com.foreach.utils.EnumUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * CodeBasedEnumHandler is an implementation of a myBatis TypeHandler
 * that facilitates persisting CodeLookup enum classes.
 */
public class CodeBasedEnumHandler<E extends Enum<E> & CodeLookup>

		extends BaseEnumHandler<E>

		implements TypeHandler
{
	protected CodeBasedEnumHandler( Class<E> clazz, E defaultValue, JdbcType customJdbcType )
	{
		super( clazz, defaultValue, customJdbcType );
	}

	protected CodeBasedEnumHandler( Class<E> clazz, E defaultValue )
	{
		this( clazz, defaultValue, null );
	}

	protected CodeBasedEnumHandler( Class<E> clazz )
	{
		this( clazz, null, null );
	}

	private E getByCode( Object code )
	{
		E e = (E) EnumUtils.getByCode( getClazz(), code );
		return ( e == null ) ? getDefaultValue() : e;
	}

	public final void setParameter(
			PreparedStatement preparedStatement, int i, Object parameter, JdbcType jdbcType ) throws SQLException
	{
		CodeLookup e = (CodeLookup) parameter;

		setJdbcParameter( preparedStatement, i, ( e != null ) ? e.getCode() : null, jdbcType );
	}

	public final Object getResult( ResultSet resultSet, String columnName ) throws SQLException
	{
		return getByCode( getParameter( resultSet, columnName ) );
	}
}
