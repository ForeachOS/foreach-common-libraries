package com.foreach.mybatis.util;

import com.foreach.utils.CodeLookup;
import com.foreach.utils.EnumUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *  <p>Abstract Utility class to facilitate persisting enums of type CodeLookup&lt;S&gt;
 *  using mybatis</p>
 *
 *  <p>Either data column where the object of type S is being persisted must correspond
 *  to the default JDBC mapping type for S, or a custom JdbcType must be provided at instantiation.</p>
 */

public abstract class CodeBasedEnumHandler<S,E extends Enum<E> & CodeLookup<S>>

		extends BaseEnumHandler<S,E>

		implements TypeHandler
{
	private E getByCode( S code )
	{
		E e = EnumUtils.getByCode( clazz, code );
		return ( e == null ) ? defaultValue : e;
	}

	protected CodeBasedEnumHandler( Class<E> clazz )
	{
		this( clazz, null, null );
	}

	protected CodeBasedEnumHandler( Class<E> clazz, E defaultValue )
	{
		this( clazz, defaultValue, null );
	}

	protected CodeBasedEnumHandler( Class<E> clazz, E defaultValue, Integer customJdbcType )
	{
		super(clazz, defaultValue, customJdbcType);
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
}
