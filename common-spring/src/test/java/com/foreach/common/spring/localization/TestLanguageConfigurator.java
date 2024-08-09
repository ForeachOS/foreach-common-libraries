/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.common.spring.localization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestLanguageConfigurator
{
	@Test
	public void notConfigured() {
		new LanguageConfigurator( (Class) null );

		assertThrows( LanguageConfigurator.LanguagesNotConfiguredException.class, LanguageConfigurator::getLanguages );
	}

	@Test
	public void notConfiguredAlternative() {
		new LanguageConfigurator( (Class) null );

		assertThrows( LanguageConfigurator.LanguagesNotConfiguredException.class, () ->LanguageConfigurator.getLanguageByCode( "en" ));
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
