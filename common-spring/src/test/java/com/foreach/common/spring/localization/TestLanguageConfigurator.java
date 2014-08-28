package com.foreach.common.spring.localization;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestLanguageConfigurator
{
	@Test(expected = LanguageConfigurator.LanguagesNotConfiguredException.class)
	public void notConfigured() {
		new LanguageConfigurator( (Class) null );

		LanguageConfigurator.getLanguages();
	}

	@Test(expected = LanguageConfigurator.LanguagesNotConfiguredException.class)
	public void notConfiguredAlternative() {
		new LanguageConfigurator( (Class) null );

		LanguageConfigurator.getLanguageByCode( "en" );
	}

	@Test
	public void validLanguageSet() {
		new LanguageConfigurator( MyLanguage.class );

		assertNotNull( LanguageConfigurator.getLanguages() );
		assertArrayEquals( MyLanguage.values(), LanguageConfigurator.getLanguages() );
	}

	@Test
	public void getLanguageByCode() {
		new LanguageConfigurator( MyLanguage.class );

		assertSame( MyLanguage.FR, LanguageConfigurator.getLanguageByCode( "fr" ) );
	}
}
