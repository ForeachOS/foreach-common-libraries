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

import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.ParameterizedType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

abstract class BaseEnumHandler<E extends Enum<E>>
{
	private Class<E> clazz;

	private E defaultValue;

	private JdbcType customJdbcType;

	private Map<String, Class<?>> map = new HashMap<String, Class<?>>();

	protected BaseEnumHandler( E defaultValue, JdbcType customJdbcType ) {
		this.clazz = getParameterClass();
		this.defaultValue = defaultValue;
		this.customJdbcType = customJdbcType;

		if ( customJdbcType != null ) {
			map.put( customJdbcType.name(), clazz );
		}
	}

	protected BaseEnumHandler( E defaultValue ) {
		this( defaultValue, null );
	}

	protected BaseEnumHandler() {
		this( null, null );
	}

	/**
	 * @return The class of enums being persisted by this TypeHandler.
	 */
	public final Class<E> getClazz() {
		return clazz;
	}

	/**
	 * returns The default enum value if one was specified at construction, null otherwise.
	 */
	public final E getDefaultValue() {
		return defaultValue;
	}

	/**
	 * returns the custom jdbc type if one was specified at construction, null otherwise.
	 */
	public final JdbcType getCustomJdbcType() {
		return customJdbcType;
	}

	/**
	 * Always throws a NotImplementedException
	 */
	public final Object getResult( CallableStatement callableStatement, int columnIndex ) throws SQLException {
		throw new UnsupportedOperationException( getClass().getName() + " does not support CallableStatements" );
	}

	/**
	 * Sets the object representing the enum at parameter index i in the prepared statement.
	 * <p/>
	 * You may not override this routine.
	 */
	protected final void setEnumParameterValue( PreparedStatement preparedStatement,
	                                            int i,
	                                            Object t,
	                                            JdbcType jdbcType ) throws SQLException {
		if ( customJdbcType == null ) {
			if ( jdbcType == null ) {
				preparedStatement.setObject( i, t );
			}
			else {
				preparedStatement.setObject( i, t, jdbcType.TYPE_CODE );
			}
		}
		else {
			preparedStatement.setObject( i, t, customJdbcType.TYPE_CODE );
		}
	}

	/**
	 * Gets the object representing the enum at the specified column in the result set.
	 * <p/>
	 * You may not override this routine.
	 */
	protected final Object getEnumParameterValue( ResultSet resultSet, String columnName ) throws SQLException {
		if ( customJdbcType == null ) {
			return resultSet.getObject( columnName );
		}
		else {
			return resultSet.getObject( columnName, map );
		}
	}

	/**
	 * Gets the object representing the enum at the specified column in the result set.
	 * <p/>
	 * You may not override this routine.
	 */
	protected final Object getEnumParameterValue( ResultSet resultSet, int columnIndex ) throws SQLException {
		if ( customJdbcType == null ) {
			return resultSet.getObject( columnIndex );
		}
		else {
			return resultSet.getObject( columnIndex, map );
		}
	}

	private Class getParameterClass() {
		ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();

		return (Class) pt.getActualTypeArguments()[0];
	}
}
