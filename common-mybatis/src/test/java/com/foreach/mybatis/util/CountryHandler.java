package com.foreach.mybatis.util;


public class CountryHandler extends CodeBasedEnumHandler<Country>
{
	@Override
	protected final Class<Country> getClazz()
	{
		return Country.class;
	}

	@Override
	protected final Country getDefaultValue()
	{
		return null;
	}
}
