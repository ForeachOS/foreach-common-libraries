package com.foreach.mybatis.enums;

import com.foreach.spring.enums.CodeLookup;
import com.foreach.spring.enums.EnumUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * CodeBasedEnumHandler is an implementation of a myBatis TypeHandler
 * that persists CodeLookup enum objects using the value obtained from getCode().
 * <p/>
 * Please note that getResult(CallableStatement cs, int columnIndex) is not implemented,
 * you can only use getResult(ResultSet rs, String columnName).
 * <p/>
 * Because myBatis creates typeHandlers by reflection, you must create a
 * CodeBasedEnumHandler subclass for each CodeLookup enum class you want to persist,
 * and provide it with a zero argument constructor, for example
 * <pre>
 *     public class CountryHandler extends CodeBasedEnumHandler&lt;Country&gt;
 *     {
 *          // superclass has a zero argument constructor
 *     }
 *
 *     public class PaymentTypeHandler extends CodeBasedEnumHandler&lt;PaymentType&gt;
 *     {
 *          public PaymentTypeHandler()
 *          {
 *              super( PaymentType.DIRECT_DEBIT );
 *          }
 *     }
 * </pre>
 * <p/>
 * If subclasses implement a method C canonicalCode(C code), it will be called before looking up an enum with a code.
 */
public abstract class CodeBasedEnumHandler<E extends Enum<E> & CodeLookup>

		extends BaseEnumHandler<E>

		implements TypeHandler
{
	private Method canonizeMethod = null;

	/**
	 * @param defaultValue   a result to be substituted when the value read from the database can't be mapped.
	 *                       This only works in one direction, a null value is always written to the database as null.
	 * @param customJdbcType a custom jdbcType to be used when reading or writing the code
	 *                       corresponding to an enum to the database.
	 */
	protected CodeBasedEnumHandler( E defaultValue, JdbcType customJdbcType )
	{
		super( defaultValue, customJdbcType );
		scanForCanonicalMethod();
	}

	/**
	 * @param defaultValue a result to be substituted when the value read from the database can't be mapped.
	 *                     This only works in one direction, a null value is always written to the database as null.
	 */
	protected CodeBasedEnumHandler( E defaultValue )
	{
		this( defaultValue, null );
	}

	protected CodeBasedEnumHandler()
	{
		this( null, null );
	}

	private void scanForCanonicalMethod()
	{
		Type[] ts = getClazz().getGenericInterfaces();

		for ( Type t : ts ) {
			if ( t instanceof ParameterizedType ) {
				ParameterizedType pt = (ParameterizedType) t;
				if ( pt.getRawType().equals( CodeLookup.class ) ) {
					Class codeClass = (Class) pt.getActualTypeArguments()[0];
					try {
						canonizeMethod:
						getClass().getMethod( "canonicalCode", codeClass );
					}
					catch ( NoSuchMethodException nsme ) {
					}
				}
			}
		}
	}

	public final void setParameter(
			PreparedStatement preparedStatement, int i, Object parameter, JdbcType jdbcType ) throws SQLException
	{
		CodeLookup e = (CodeLookup) parameter;

		setCodeParameter( preparedStatement, i, ( e != null ) ? e.getCode() : null, jdbcType );
	}

	public final Object getResult( ResultSet resultSet, String columnName ) throws SQLException
	{
		return getByCode( getCodeParameter( resultSet, columnName ) );
	}

	private E getByCode( Object code )
	{
		if ( canonizeMethod != null ) {
			try {
				canonizeMethod.invoke( this, code );
			}
			catch ( IllegalAccessException e ) {
			}
			catch ( InvocationTargetException e ) {
			}
		}

		E e = (E) EnumUtils.getByCode( getClazz(), code );
		return ( e == null ) ? getDefaultValue() : e;
	}
}
