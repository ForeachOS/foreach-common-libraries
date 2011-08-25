package com.foreach.mybatis.util;

import com.foreach.utils.EnumUtils;
import com.foreach.utils.IdLookup;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * IdBasedEnumHandler is an implementation of a myBatis TypeHandler
 * that facilitates persisting IdLookup enum classes.
 */
public class IdBasedEnumHandler<E extends Enum<E> & IdLookup>

		extends BaseEnumHandler<E>

		implements TypeHandler
{
	protected IdBasedEnumHandler( Class<E> clazz, E defaultValue, JdbcType customJdbcType )
	{
		super( clazz, defaultValue, customJdbcType );
	}

	protected IdBasedEnumHandler( Class<E> clazz, E defaultValue )
	{
		this( clazz, defaultValue, null );
	}

	protected IdBasedEnumHandler( Class<E> clazz  )
	{
		this( clazz, null, null );
	}

	protected final E getById( Object id )
	{
		E e = (E) EnumUtils.getById( getClazz(), id );
		return ( e == null ) ? getDefaultValue() : e;
	}

	public final void setParameter(
			PreparedStatement preparedStatement, int i, Object parameter, JdbcType jdbcType ) throws SQLException
	{
		IdLookup e = (IdLookup) parameter;

		setJdbcParameter( preparedStatement, i, ( e != null ) ? e.getId() : null, jdbcType );
	}

	public final E getResult( ResultSet resultSet, String columnName ) throws SQLException
	{
		return getById( getParameter( resultSet, columnName ) );
	}
}
