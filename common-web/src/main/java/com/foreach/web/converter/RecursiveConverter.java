package com.foreach.web.converter;

import org.springframework.core.convert.ConversionService;

public interface RecursiveConverter
{
	void setConversionService( ConversionService conversionService );
}
