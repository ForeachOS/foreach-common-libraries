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
 * <p>
 * Because myBatis creates typeHandlers by reflection, you must create an
 * IdBasedEnumHandler subclass for each IdLookup enum class you want to persist,
 * and provide it with a zero argument constructor, for example
 * <pre>
 *     public class CountryHandler extends IdBasedEnumHandler<Country>
 *     {
 *          // superclass has a zero argument constructor
 *     }
 *
 *     public class PaymentTypeHandler extends IdBasedEnumHandler<PaymentType>
 *     {
 *          public PaymentTypeHandler()
 *          {
 *              super( PaymentType.DIRECT_DEBIT );
 *          }
 *     }
 * </pre>
 */
public abstract class IdBasedEnumHandler<E extends Enum<E> & IdLookup>

		extends BaseEnumHandler<E>

		implements TypeHandler
{
	protected IdBasedEnumHandler( E defaultValue, JdbcType customJdbcType )
	{
		super( defaultValue, customJdbcType );
	}

	protected IdBasedEnumHandler( E defaultValue )
	{
		this( defaultValue, null );
	}

	protected IdBasedEnumHandler( )
	{
		this( null, null );
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

	protected final E getById( Object id )
	{
		E e = (E) EnumUtils.getById( getClazz(), id );
		return ( e == null ) ? getDefaultValue() : e;
	}
}
