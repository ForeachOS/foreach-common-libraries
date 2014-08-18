package com.foreach.common.test.web.converter;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.Resource;

public class TestSpringWorks extends BaseConversionServiceTest
{
	@Test
	public void weConvertEnumsButNotResources() {
		Assert.assertEquals( true, conversionService.canConvert( String.class, Enum.class ) );
		Assert.assertEquals( false, conversionService.canConvert( String.class, Resource.class ) );
	}
}
