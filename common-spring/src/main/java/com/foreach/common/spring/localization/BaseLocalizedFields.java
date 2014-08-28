package com.foreach.common.spring.localization;

/**
 * Provides an abstract base class that implements the LocalizedFields interface and allows
 * Language setting upon construction of the fields instance.
 */
public abstract class BaseLocalizedFields implements LocalizedFields
{
	private final Language language;

	public BaseLocalizedFields( Language language ) {
		this.language = language;
	}

	/**
	 * @return Language these fields are linked to.
	 */
	public final Language getLanguage() {
		return language;
	}
}
