package com.foreach.test.web.convertor;

import com.foreach.web.convertor.EnumCodeRenderer;
import com.foreach.web.convertor.EnumConverterFactory;
import com.foreach.web.convertor.EnumIdRenderer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.Resource;

import java.util.HashSet;
import java.util.Set;

public class TestSpringWorks extends BaseConversionServiceTest
{
	@Test
	public void weConvertEnumsButNotResources()
	{
		Assert.assertEquals( true, conversionService.canConvert( String.class, Enum.class ) );
		Assert.assertEquals( false, conversionService.canConvert( String.class, Resource.class ) );
	}
}
