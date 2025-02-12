/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.common.mybatis.enums;

import com.foreach.common.spring.enums.EnumUtils;
import com.foreach.common.spring.enums.IdLookup;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * IdBasedEnumHandler is an implementation of a myBatis TypeHandler
 * that persists IdLookup enum objects using the value obtained by getId().
 * <p/>
 * Please note that getResult(CallableStatement cs, int columnIndex) is not implemented,
 * you can only use getResult(ResultSet rs, String columnName).
 * <p/>
 * Because myBatis creates typeHandlers by reflection, you must create an
 * IdBasedEnumHandler subclass for each IdLookup enum class you want to persist,
 * and provide it with a zero argument constructor, for example
 * <pre>
 *     public class CountryHandler extends IdBasedEnumHandler&lt;Country&gt;
 *     {
 *          // superclass has a zero argument constructor
 *     }
 *
 *     public class PaymentTypeHandler extends IdBasedEnumHandler&lt;PaymentType&gt;
 *     {
 *          public PaymentTypeHandler()
 *          {
 *              super( PaymentType.DIRECT_DEBIT );
 *          }
 *     }
 * </pre>
 * </p>
 */
public abstract class IdBasedEnumHandler<E extends Enum<E> & IdLookup> extends BaseEnumHandler<E> implements TypeHandler
{
	/**
	 * @param defaultValue   a result to be substituted when the value read from the database can't be mapped.
	 *                       This only works in one direction, a null value is always written to the database as null.
	 * @param customJdbcType a custom jdbcType to be used when reading or writing the id
	 *                       corresponding to an enum to the database.
	 */
	protected IdBasedEnumHandler( E defaultValue, JdbcType customJdbcType ) {
		super( defaultValue, customJdbcType );
	}

	/**
	 * @param defaultValue a result to be substituted when the value read from the database can't be mapped.
	 *                     This only works in one direction, a null value is always written to the database as null.
	 */
	protected IdBasedEnumHandler( E defaultValue ) {
		this( defaultValue, null );
	}

	protected IdBasedEnumHandler() {
		this( null, null );
	}

	public final void setParameter( PreparedStatement preparedStatement,
	                                int i,
	                                Object parameter,
	                                JdbcType jdbcType ) throws SQLException {
		IdLookup e = (IdLookup) parameter;

		setEnumParameterValue( preparedStatement, i, ( e != null ) ? e.getId() : null, jdbcType );
	}

	public final E getResult( ResultSet resultSet, String columnName ) throws SQLException {
		return getById( getEnumParameterValue( resultSet, columnName ) );
	}

	public final Object getResult( ResultSet resultSet, int columnIndex ) throws SQLException {
		return getById( getEnumParameterValue( resultSet, columnIndex ) );
	}

	protected final E getById( Object id ) {
		E e = (E) EnumUtils.getById( getClazz(), id );
		return ( e == null ) ? getDefaultValue() : e;
	}
}
