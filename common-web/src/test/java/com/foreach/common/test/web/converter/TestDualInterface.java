package com.foreach.common.test.web.converter;

import com.foreach.common.spring.enums.CodeLookup;
import com.foreach.common.spring.enums.IdLookup;
import org.junit.Assert;
import org.junit.Test;

public class TestDualInterface extends BaseConversionServiceTest
{

	@Test
	public void useCodeLookup() {
		Assert.assertEquals( Foo.FOO, conversionService.convert( "foo", Foo.class ) );
		Assert.assertEquals( Foo.BOZ, conversionService.convert( "7", Foo.class ) );
	}

	public enum Foo implements CodeLookup<String>, IdLookup<Integer>
	{
		FOO( 1, "foo" ),
		BAR( 3, "bar" ),
		BOZ( 7, "boz" );

		private int id;
		private String code;

		Foo( int id, String code ) {
			this.id = id;
			this.code = code;
		}

		public Integer getId() {
			return id;
		}

		public String getCode() {
			return code;
		}
	}
}
