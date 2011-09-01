package com.foreach.web.convertor;

import com.foreach.spring.enums.IdLookup;
import org.springframework.core.convert.converter.Converter;

public class EnumIdRenderer<E extends Enum<E>&IdLookup<Integer>> implements Converter<E,Integer>
{
	public Integer convert(E source)
	{
		return source.getId();
	}
}
