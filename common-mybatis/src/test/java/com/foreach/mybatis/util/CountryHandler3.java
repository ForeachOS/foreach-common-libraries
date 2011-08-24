package com.foreach.mybatis.util;

import java.sql.Types;

public class CountryHandler3 extends IdBasedEnumHandler<Long, Country>
{
	public CountryHandler3()
	{
		super( Country.class, null, Types.DECIMAL );
	}
}
