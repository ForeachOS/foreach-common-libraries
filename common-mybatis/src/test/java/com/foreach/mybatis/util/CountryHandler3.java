package com.foreach.mybatis.util;

import org.apache.ibatis.type.JdbcType;

public class CountryHandler3 extends IdBasedEnumHandler<Long, Country>
{
	public CountryHandler3()
	{
		super( Country.class, null, JdbcType.DECIMAL );
	}
}
