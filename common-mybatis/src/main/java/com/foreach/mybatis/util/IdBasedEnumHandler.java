package com.foreach.mybatis.util;

import org.apache.commons.lang.NotImplementedException;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class IdBasedEnumHandler<I, E extends Enum<E> & IdLookup<I>> implements TypeHandler
{
	protected abstract Class<E> getClazz();

	protected abstract E getDefaultValue();

	protected final E getById( I id )
	{
		E e = EnumUtils.getById( getClazz(), id );
		return ( e == null ) ? getDefaultValue() : e;
	}

	public final void setParameter(
			PreparedStatement preparedStatement, int i, Object parameter, JdbcType jdbcType ) throws SQLException
	{
		IdLookup e = ( parameter == null ) ? getDefaultValue() : (IdLookup) parameter;

		setParameter( preparedStatement, i, e );
	}

	protected abstract void setParameter( PreparedStatement preparedStatement, int i, IdLookup e );

	public abstract Object getResult( ResultSet resultSet, String columnName ) throws SQLException;


	public final Object getResult( CallableStatement callableStatement, int columnIndex ) throws SQLException
	{
		throw new NotImplementedException( getClass().getName()+" does not support CallableStatements" );
	}
}
