package com.foreach.mybatis.util;

import com.foreach.utils.CodeLookup;
import com.foreach.utils.LongIdLookup;

public enum Country implements CodeLookup, LongIdLookup
{
	AU( "Aus", 100000001L),
	ZB( "Zim", 100000002L);

	private String name;
	private long largeNumber;

	Country(String name, long largeNumber)
	{
		this.name = name;
		this.largeNumber = largeNumber;
	}

	public String getCode()
	{
		return name;
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
			if ( c.getId() == id ) {
				return c;
			}
		}

		return null;
	}


}
