package com.foreach.mybatis.util;

public class CountryHandler2 extends IdBasedEnumHandler<Long, Country>
{
	public CountryHandler2()
	{
		super( Country.class, null, null );
	}
}
