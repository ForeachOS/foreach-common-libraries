package com.foreach.mybatis.util;


public class CountryHandler extends CodeBasedEnumHandler<String,Country>
{
	public CountryHandler()
	{
		super( Country.class, null, null );
	}
}
