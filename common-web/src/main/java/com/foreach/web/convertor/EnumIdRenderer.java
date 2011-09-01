package com.foreach.web.convertor;

import com.foreach.spring.enums.IdLookup;
import org.springframework.core.convert.converter.Converter;

public class EnumIdRenderer<E extends Enum<E>&IdLookup<I>,I> implements Converter<E,I>
{
	public I convert(E source)
	{
		return source.getId();
	}
}
