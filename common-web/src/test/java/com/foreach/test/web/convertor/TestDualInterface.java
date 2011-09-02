package com.foreach.test.web.convertor;

import com.foreach.spring.enums.CodeLookup;
import com.foreach.spring.enums.IdLookup;
import com.foreach.web.convertor.CustomConversionServiceFactoryBean;
import com.foreach.web.convertor.EnumCodeRenderer;
import com.foreach.web.convertor.EnumConverterFactory;
import com.foreach.web.convertor.EnumIdRenderer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;

import java.util.HashSet;
import java.util.Set;

public class TestDualInterface extends BaseConversionServiceTest
{

	@Test
	public void useCodeLookup()
	{
		Assert.assertEquals(  Foo.FOO, conversionService.convert( "foo", Foo.class ) );
		Assert.assertEquals(  Foo.BOZ, conversionService.convert( "7", Foo.class ) );
	}

	public enum Foo implements CodeLookup<String>, IdLookup<Integer>
	{
		FOO(1,"foo"),
		BAR(3,"bar"),
		BOZ(7,"boz");

		private int id;
		private String code;

		Foo(int id, String code )
		{
			this.id = id;
			this.code =code;
		}

		public Integer getId()
		{
			return id;
		}

		public String getCode()
		{
			return code;
		}
	}
}
