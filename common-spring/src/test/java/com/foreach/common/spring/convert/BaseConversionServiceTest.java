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

import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.convert.ConversionService;

import java.util.HashSet;
import java.util.Set;

public class BaseConversionServiceTest
{
	protected ConversionService conversionService;

	@BeforeEach
	public void prepareTest() {
		CustomConversionServiceFactoryBean factory = new CustomConversionServiceFactoryBean();
		Set<Object> converters = new HashSet<Object>();

		EnumConverterFactory converterFactory = new EnumConverterFactory();

		converters.add( converterFactory );
		converters.add( new EnumIdRenderer() );
		converters.add( new EnumCodeRenderer() );

		factory.setConverters( converters );
		factory.afterPropertiesSet();

		conversionService = factory.getObject();
	}
}
