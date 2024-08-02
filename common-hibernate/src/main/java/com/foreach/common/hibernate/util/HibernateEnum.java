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
package com.foreach.common.hibernate.util;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.BasicType;
import org.hibernate.type.TypeFactory;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.spi.TypeConfiguration;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * <p>This class converts the string value from the resultset to Java Enum and vice versa.</p>
 * <p>
 * Usage Eg - To map a java enum object Role, define the following mapping in hbm file.
 * <pre>
 *     &lt;typedef class="com.foreach.hibernate.enums.HibernateEnum" name="roleType"&gt;
 *          &lt;param name="enumClassName"&gt;com.xxx.xxx.Role&lt;/param&gt;
 *     &lt;/typedef&gt;
 *  </pre>
 * </p>
 */
// Improvement: -Looks like this can be simplified for the end user using reflection, as in this example:
// http://community.jboss.org/wiki/Java5StringValuedEnumUserType

public class HibernateEnum implements UserType, ParameterizedType
{
	private static final String DEFAULT_IDENTIFIER_METHOD_NAME = "getId";
	private static final String DEFAULT_VALUE_OF_METHOD_NAME = "getById";

	/*
		Sonar will bitch, because Hibernate will construct instances using
		a zero argument constructor and the method setParameterValues,
		so many instance variables remain null after construction.
	*/

	private Class enumClass;
	private Method identifierMethod;
	private Method valueOfMethod;
	private BasicType type;
	private int[] sqlTypes;

	public HibernateEnum() {
		// an empty constructor setting member variables to null to dodge sonar from reporting
		// "Variables not initialized inside constructor"
		this.enumClass = null;
		this.identifierMethod = null;
		this.valueOfMethod = null;
		this.type = null;
		this.sqlTypes = null;
	}

	public final void setParameterValues( Properties parameters ) {
		String enumClassName = parameters.getProperty( "enumClassName" );
		Class identifierType;

		try {
			enumClass = Class.forName( enumClassName ).asSubclass( Enum.class );
		}
		catch ( ClassNotFoundException cfne ) {
			throw new HibernateException( "Enum class not found", cfne );
		}
		String identifierMethodName = parameters.getProperty( "identifierMethod", DEFAULT_IDENTIFIER_METHOD_NAME );
		try {
			identifierMethod = enumClass.getMethod( identifierMethodName );
			identifierType = identifierMethod.getReturnType();
		}
		catch ( Exception e ) {
			throw new HibernateException( "Failed to obtain identifier method", e );
		}
		type =  new TypeConfiguration().getTypeResolver().basic( identifierType.getName() );
		if ( type == null ) {
			throw new HibernateException( "Unsupported identifier type " + identifierType.getName() );
		}

		sqlTypes = new int[] { type.sqlTypes(null)[0] };
		String valueOfMethodName = parameters.getProperty( "valueOfMethod", DEFAULT_VALUE_OF_METHOD_NAME );
		try {
			valueOfMethod = enumClass.getMethod( valueOfMethodName, identifierType );
		}
		catch ( Exception e ) {
			throw new HibernateException( "Failed to obtain valueOf method", e );
		}
	}

	public final Class returnedClass() {
		return enumClass;
	}

	@Override
	public final Object nullSafeGet( ResultSet rs,
	                                 String[] names,
	                                 SharedSessionContractImplementor session,
	                                 Object owner ) throws SQLException {
		Object identifier = type.nullSafeGet( rs, names[0] ,session,owner);
		if ( rs.wasNull() ) {
			return null;
		}

		StringBuffer errorMsg = new StringBuffer( "Exception while invoking valueOf method \"" );
		try {
			errorMsg.append( valueOfMethod.getName() ).append( "\" of " ).append( "enumeration class \"" ).append(
					enumClass ).append( '\"' );

			return valueOfMethod.invoke( enumClass, identifier );
		}
		catch ( IllegalAccessException exc ) {
			throw new HibernateException( errorMsg.toString(), exc );
		}
		catch ( InvocationTargetException exc ) {
			throw new HibernateException( errorMsg.toString(), exc );
		}
	}

	@Override
	public final void nullSafeSet( PreparedStatement st,
	                               Object value,
	                               int index,
	                               SharedSessionContractImplementor session ) throws SQLException {
		StringBuffer errorMsg = new StringBuffer( "Exception while invoking identifierMethod method \"" );
		try {
			errorMsg.append( identifierMethod.getName() ).append( "\" of " ).append( "enumeration class \"" ).append(
					enumClass ).append( '\"' );

			if ( value == null ) {
				st.setNull( index, type.sqlTypes(null)[0] );
			}
			else {
				Object identifier = identifierMethod.invoke( value );
				type.nullSafeSet( st, identifier, index, session );
			}
		}
		catch ( IllegalAccessException | InvocationTargetException exc ) {
			throw new HibernateException( errorMsg.toString(), exc );
		}
	}

	public final int[] sqlTypes() {
		return sqlTypes;
	}

	public final Object assemble( Serializable cached, Object owner ) {
		return cached;
	}

	public final Object deepCopy( Object value ) {
		return value;
	}

	public final Serializable disassemble( Object value ) {
		return (Serializable) value;
	}

	// Enum equallity IS pointer equality
	@SuppressWarnings("all")
	public final boolean equals( Object x, Object y ) {
		if ( x == null ) {
			return ( y == null );
		}
		return x.equals( y );
	}

	public final int hashCode( Object x ) {
		return x.hashCode();
	}

	public final boolean isMutable() {
		return false;
	}

	public final Object replace( Object original, Object target, Object owner ) {
		return original;
	}
}
