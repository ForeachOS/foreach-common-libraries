package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.Language;

import java.util.Locale;

public enum TestLanguage implements Language
{
	EN( "en", "English", Locale.ENGLISH ),
	NL( "nl", "Nederlands", Locale.US);

	private String code, name;
	private Locale locale;

	TestLanguage(String code, String name, Locale locale) {
		this.code = code;
		this.name = name;
		this.locale = locale;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public Locale getLocale() {
		return locale;
	}
}
