package com.foreach.spring.localization;

import java.util.Locale;

public enum MyLanguage implements Language
{
	EN( "en", "English", Locale.ENGLISH ),
	FR( "fr", "Français", Locale.FRENCH );

	private String code, name;
	private Locale locale;

	MyLanguage( String code, String name, Locale locale )
	{
		this.code = code;
		this.name = name;
		this.locale = locale;
	}

	public String getCode()
	{
		return code;
	}

	public String getName()
	{
		return name;
	}

	public Locale getLocale()
	{
		return locale;
	}
}
