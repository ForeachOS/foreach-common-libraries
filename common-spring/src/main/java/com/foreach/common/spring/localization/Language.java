package com.foreach.common.spring.localization;

import com.foreach.common.spring.enums.CodeLookup;

import java.util.Locale;

/**
 * Interface for custom Language implementation.  A language is always represented by a unique
 * language code.
 */
public interface Language extends CodeLookup<String>
{
	/**
	 * @return Descriptive name of the language.
	 */
	String getName();

	/**
	 * @return Java Locale instance linked to this language.
	 */
	Locale getLocale();
}
