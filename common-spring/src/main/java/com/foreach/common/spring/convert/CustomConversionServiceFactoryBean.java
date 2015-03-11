package com.foreach.common.spring.convert;

import org.springframework.context.support.ConversionServiceFactoryBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class adds support for recursive converters:
 * </p>
 * All converters implementing the RecursiveConverter interface will be given a
 * link back to the singleton conversion service.
 * </p>
 * If you create this object manually, you must call the afterPropertiesSet method.
 */
public class CustomConversionServiceFactoryBean extends ConversionServiceFactoryBean
{
	private transient List<RecursiveConverter> recursiveConverters = new ArrayList<RecursiveConverter>();

	public final void setConverters( Set<?> converters ) {

		super.setConverters( converters );

		for ( Object converter : converters ) {
			if ( converter instanceof RecursiveConverter ) {
				recursiveConverters.add( (RecursiveConverter) converter );
			}
		}
	}

	@Override
	public final void afterPropertiesSet() {
		super.afterPropertiesSet();

		// We process recursive converters and give them a link back to the service
		for ( RecursiveConverter converter : recursiveConverters ) {
			converter.setConversionService( getObject() );
		}
	}
}
