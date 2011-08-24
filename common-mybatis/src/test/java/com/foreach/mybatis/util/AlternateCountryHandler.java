package com.foreach.mybatis.util;

public class AlternateCountryHandler extends IdBasedEnumHandler<Long, Country>
{
	public AlternateCountryHandler()
	{
		super( Country.class, null, null );
	}
}
