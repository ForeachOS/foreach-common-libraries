package com.foreach.web.convertor;

import com.foreach.spring.enums.CodeLookup;
import org.springframework.core.convert.converter.Converter;

public class EnumCodeRenderer<E extends Enum<E>&CodeLookup<S>,S> implements Converter<E,S>
{
	public S convert(E source)
	{
		return source.getCode();
	}
}
