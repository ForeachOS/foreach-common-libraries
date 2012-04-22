package com.foreach.spring.localization;

import com.foreach.spring.enums.CodeLookup;

import java.util.Locale;

public interface Language extends CodeLookup<String>
{
	String getName();

	Locale getLocale();
}
