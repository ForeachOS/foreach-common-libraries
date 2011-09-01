package com.foreach.web.convertor;

import org.springframework.core.convert.ConversionService;

public interface RecursiveConverter
{
	void setConversionService( ConversionService conversionService );
}
