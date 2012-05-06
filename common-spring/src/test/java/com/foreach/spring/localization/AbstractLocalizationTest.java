package com.foreach.spring.localization;

import org.junit.BeforeClass;

public abstract class AbstractLocalizationTest
{
	@BeforeClass
	public static void initializeLanguageSystem()
	{
		new LanguageConfigurator( MyLanguage.class );
	}
}
