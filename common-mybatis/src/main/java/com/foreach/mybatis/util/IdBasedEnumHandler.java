package com.foreach.mybatis.util;

import com.foreach.utils.EnumUtils;
import com.foreach.utils.IdLookup;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *  Abstract Utility class to facilitate persisting enums of type IdLookup&lt;I&gt;
 *  using mybatis
 *
 *  <p>Either data column where the object of type I is being persisted must correspond
 *  to the default JDBC mapping type for I, or a custom JdbcType must be provided at instantiation.</p>
 */

public abstract class IdBasedEnumHandler<I, E extends Enum<E> & IdLookup<I>>

		extends BaseEnumHandler<I,E>

		implements TypeHandler
{
	protected final E getById( I id )
	{
		E e = EnumUtils.getById( clazz, id );
		return ( e == null ) ? defaultValue : e;
	}

	protected IdBasedEnumHandler( Class<E> clazz )
	{
		this( clazz, null, null );
	}

	protected IdBasedEnumHandler( Class<E> clazz, E defaultValue )
	{
		this( clazz, defaultValue, null );
	}

	protected IdBasedEnumHandler( Class<E> clazz, E defaultValue, JdbcType customJdbcType )
	{
		super(clazz, defaultValue, customJdbcType);
	}

	public final void setParameter(
			PreparedStatement preparedStatement, int i, Object parameter, JdbcType jdbcType ) throws SQLException
	{
		IdLookup<I> e = ( IdLookup<I> ) parameter;

		setJdbcParameter( preparedStatement, i, (e != null)? e.getId(): null, jdbcType );
	}

	public final E getResult( ResultSet resultSet, String columnName ) throws SQLException
	{
		return getById( getParameter( resultSet, columnName) );
	}
}
