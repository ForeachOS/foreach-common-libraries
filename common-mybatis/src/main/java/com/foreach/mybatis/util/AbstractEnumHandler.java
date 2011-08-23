package com.foreach.mybatis.util;

import org.apache.commons.lang.NotImplementedException;

import java.sql.CallableStatement;
import java.sql.SQLException;

public abstract class AbstractEnumHandler<E extends Enum<E>>
{
	protected abstract Class<E> getClazz();

	protected abstract E getDefaultValue();


	public final Object getResult( CallableStatement callableStatement, int columnIndex ) throws SQLException
	{
		throw new NotImplementedException( getClass().getName()+" does not support CallableStatements" );
	}

}
