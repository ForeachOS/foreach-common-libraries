package com.foreach.common.test.web.converter;

import com.foreach.common.web.converter.CustomConversionServiceFactoryBean;
import com.foreach.common.web.converter.EnumCodeRenderer;
import com.foreach.common.web.converter.EnumConverterFactory;
import com.foreach.common.web.converter.EnumIdRenderer;
import org.junit.Before;
import org.springframework.core.convert.ConversionService;

import java.util.HashSet;
import java.util.Set;

public class BaseConversionServiceTest
{
	protected ConversionService conversionService;

	@Before
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
