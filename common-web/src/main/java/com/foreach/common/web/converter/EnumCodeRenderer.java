package com.foreach.common.web.converter;

import com.foreach.common.spring.enums.CodeLookup;
import org.springframework.core.convert.converter.Converter;

public class EnumCodeRenderer<E extends Enum<E> & CodeLookup<String>> implements Converter<E, String>
{
	public final String convert( E source ) {
		return source.getCode();
	}
}
