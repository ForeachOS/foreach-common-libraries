package com.foreach.test.mybatis.util;

import com.foreach.spring.enums.CodeLookup;
import com.foreach.spring.enums.IdLookup;

public enum Country implements CodeLookup<String>, IdLookup<Long>
{
	AU( "aus", 100000001L ),
	ZB( "zim", 100000002L );

	private String code;
	private long largeNumber;

	Country( String code, long largeNumber )
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
