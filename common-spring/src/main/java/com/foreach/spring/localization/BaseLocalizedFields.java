package com.foreach.spring.localization;

public abstract class BaseLocalizedFields implements LocalizedFields
{
	private Language language;

	public BaseLocalizedFields( Language language )
	{
		this.language = language;
	}

	public final Language getLanguage()
	{
		return language;
	}

	public final void setLanguage( Language language )
	{
		this.language = language;
	}
}