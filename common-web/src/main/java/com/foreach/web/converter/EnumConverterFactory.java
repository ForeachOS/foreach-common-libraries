package com.foreach.web.converter;

import com.foreach.spring.enums.CodeLookup;
import com.foreach.spring.enums.EnumUtils;
import com.foreach.spring.enums.IdLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * EnumConverterFactory is an implementation of the Spring ConverterFactory interface
 * that generates Converters from String to Enum classes implementing IdLookup or CodeLookup.
 * <p/>
 * The conversion is a two step process: first a String is converted to the parameter type of
 * IdLookup or CodeLookup, then that value is used to determine the enum with that id or code.
 * <p/>
 * Each EnumConverterFactory instance must be provided with a conversion service
 * that can convert String to all the parameter types that require conversion.
 * <p/>
 * When converting to an Enum class implementing both IdLookup and CodeLookup,
 * the IdLookup is attempted first.
 * <p/>
 * In most cases, you will group your converters in a single conversion service.
 * Check <a href="http://static.springsource.org/spring/docs/3.0.0.RC3/spring-framework-reference/html/ch05s05.html">the spring documentation</a>
 * on how to configure a conversion service for your application.
 * <p/>If an object can't be converted with the conversion service, the system falls back to property editors.
 * For this reason, you should keep all your converters as specific as possible.
 * <p>
 *     To use the EnumConverterFactory in Spring 3.1+ with @Configuration annotations:
 *     <pre>
 *         public class WebConfig extends WebMvcConfigurerAdapter
 *         {
*             {@literal @}Override
*             public void addFormatters( FormatterRegistry registry ) {
*               registry.addConverterFactory( new EnumConverterFactory() );
*             }
 *         }
 *     </pre>
 * </p>
 */

public class EnumConverterFactory implements ConverterFactory<String, Enum>, RecursiveConverter
{
	private Logger logger = LoggerFactory.getLogger( getClass() );

	// Try to not get in an infinite loop here...

	private ConversionService conversionService;

	/**
	 * Set the conversionService. This service must be able to convert String to all
	 * the parameter types used.
	 */
	public final void setConversionService( ConversionService conversionService ) {
		this.conversionService = conversionService;
	}

	/**
	 * Get a converter instance for the specified enum class.
	 *
	 * @param targetType the Enum class being converted to.
	 * @return a converter implementing the Spring Converter interface
	 *         that converts String to the specified enum class.
	 */
	public final <E extends Enum> Converter<String, E> getConverter( Class<E> targetType ) {
		logger.debug( "converter requested for type " + targetType.getName() );

		if ( conversionService == null ) {
			logger.error( "conversionService not set for EnumConverterFactory instance" );
		}

		return new EnumConverter( targetType );
	}

	private final class EnumConverter<E extends Enum> implements Converter<String, E>
	{

		private Class<E> enumType;

		public EnumConverter( Class<E> enumType ) {
			this.enumType = enumType;
		}

		public E convert( String source ) {
			if ( IdLookup.class.isAssignableFrom( enumType ) ) {

				logger.debug( "attempting to convert " + source + " to " + enumType + " using IdLookup" );

				Class intermediateType = lookupMethodParameterClass( enumType, IdLookup.class );

				if ( intermediateType == null ) {
					logger.error( "IdLookup parameter type not specified, assuming Integer." );
					intermediateType = Integer.class;
				}

				E attempt = tryConvertUsingMethod( source, intermediateType, "getById" );

				if ( attempt != null ) {
					return attempt;
				}
			}

			if ( CodeLookup.class.isAssignableFrom( enumType ) ) {

				logger.debug( "attempting to convert " + source + " to " + enumType + " using CodeLookup" );

				Class intermediateType = lookupMethodParameterClass( enumType, CodeLookup.class );

				if ( intermediateType == null ) {
					logger.error( "CodeLookup parameter type not specified, assuming String." );
					intermediateType = String.class;
				}

				E attempt = tryConvertUsingMethod( source, intermediateType, "getByCode" );

				if ( attempt != null ) {
					return attempt;
				}
			}

			return null;
		}

		/**
		 * Find the type parameter of the argument class for the specified lookupInterface,
		 * so for Foo implements LookUp<Bar>, return Bar.Class;
		 */
		private Class lookupMethodParameterClass( Class targetClass, Class lookupInterface ) {
			Type[] ts = targetClass.getGenericInterfaces();

			for ( Type t : ts ) {
				if ( t instanceof ParameterizedType ) {
					ParameterizedType pt = (ParameterizedType) t;
					if ( pt.getRawType().equals( lookupInterface ) ) {
						return (Class) pt.getActualTypeArguments()[0];
					}
				}
			}

			return null;
		}

		private E tryConvertUsingMethod( String source, Class intermediateType, String lookupMethodName ) {
			try {

				Object id = source;

				logger.debug( "performing intermediate conversion of " + source + " to " + intermediateType );

				if ( !String.class.isAssignableFrom( intermediateType ) ) {
					id = conversionService.convert( source, TypeDescriptor.valueOf( String.class ),
					                                TypeDescriptor.valueOf( intermediateType ) );
				}

				Method m = com.foreach.spring.enums.EnumUtils.class.getMethod( lookupMethodName, Class.class,
				                                                               Object.class );

				return (E) m.invoke( EnumUtils.class, enumType, id );
			}
			catch ( NoSuchMethodException nsme ) {
				logger.error( nsme.getMessage(), nsme );
			}
			catch ( IllegalAccessException iae ) {
				logger.error( iae.getMessage(), iae );
			}
			catch ( InvocationTargetException ite ) {
				logger.error( ite.getMessage(), ite );
			}
			catch ( ConversionFailedException ce ) {
				// this is allowed if both interfaces are implemented
			}

			logger.error( "intermediate conversion failed" );
			return null;
		}
	}
}

