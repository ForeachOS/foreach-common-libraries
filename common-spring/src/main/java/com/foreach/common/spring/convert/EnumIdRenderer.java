package com.foreach.common.spring.convert;

import com.foreach.common.spring.enums.IdLookup;
import org.springframework.core.convert.converter.Converter;

public class EnumIdRenderer<E extends Enum<E> & IdLookup<Integer>> implements Converter<E, Integer>
{
	public final Integer convert( E source ) {
		return source.getId();
	}
}
