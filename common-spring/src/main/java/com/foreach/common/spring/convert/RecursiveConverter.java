package com.foreach.common.spring.convert;

import org.springframework.core.convert.ConversionService;

public interface RecursiveConverter
{
	void setConversionService( ConversionService conversionService );
}
