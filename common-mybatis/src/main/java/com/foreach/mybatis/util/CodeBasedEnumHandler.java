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
 * <p>
 * Because myBatis creates typeHandlers by reflection, you must create a
 * CodeBasedEnumHandler subclass for each CodeLookup enum class you want to persist,
 * and provide it with a zero argument constructor, for example
 * <pre>
 *     public class CountryHandler extends CodeBasedEnumHandler<Country>
 *     {
 *          // superclass has a zero argument constructor
 *     }
 *
 *     public class PaymentTypeHandler extends CodeBasedEnumHandler<PaymentType>
 *     {
 *          public PaymentTypeHandler()
 *          {
 *              super( PaymentType.DIRECT_DEBIT );
 *          }
 *     }
 * </pre>
 * </p>
 */
public abstract class CodeBasedEnumHandler<E extends Enum<E> & CodeLookup>

		extends BaseEnumHandler<E>

		implements TypeHandler
{
	protected CodeBasedEnumHandler( E defaultValue, JdbcType customJdbcType )
	{
		super( defaultValue, customJdbcType );
	}

	protected CodeBasedEnumHandler( E defaultValue )
	{
		this( defaultValue, null );
	}

	protected CodeBasedEnumHandler()
	{
		this( null, null );
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

	private E getByCode( Object code )
	{
		E e = (E) EnumUtils.getByCode( getClazz(), code );
		return ( e == null ) ? getDefaultValue() : e;
	}
}
