package com.foreach.mybatis.util;

import com.foreach.utils.CodeLookup;
import com.foreach.utils.IdLookup;

public enum Country implements CodeLookup<String>, IdLookup<Long>
{
	AU( "Aus", 100000001L),
	ZB( "Zim", 100000002L);

	private String code;
	private long largeNumber;

	Country(String name, long largeNumber)
	{
		this.code = code;
		this.largeNumber = largeNumber;
	}

	public String getCode()
	{
		return code;
	}

	public Long getId()
	{
		return largeNumber;
	}

	public static final Country getByCode( String code )
	{
		for ( Country c : values() ) {
			if ( c.getCode().equals( code ) ) {
				return c;
			}
		}

		return null;
	}

	public static final Country getById( Long id )
	{
		for ( Country c : values() ) {
			if ( c.getId().equals( id ) ) {
				return c;
			}
		}

		return null;
	}


}
