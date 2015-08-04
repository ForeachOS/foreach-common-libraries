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
