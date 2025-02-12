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

import com.foreach.common.spring.enums.EnumUtils;

/**
 * LanguageConfigurator holds the reference to the actual Language implementation being used.
 * Creating a LanguageConfigurator is required before it is possible to make use of the localization system.
 * LanguageConfigurator is in fact a sort of singleton approach, but the singleton reference can be set
 * by simply constructing a LanguageConfigurator bean with the implementing Language class as parameter.
 */
@SuppressWarnings("all")
public final class LanguageConfigurator
{
	private static Class languageClass;

	/**
	 * Initializes the Language system using the specified enum class implementing the Language interface.
	 *
	 * @param languageClass Class that is an Enum and implements the Language interface.
	 * @param <E>           Type parameter for Enum and Language interface.
	 */
	public <E extends Enum<E> & Language> LanguageConfigurator( Class<E> languageClass ) {
		LanguageConfigurator.languageClass = languageClass;
	}

	/**
	 * @param code Code to lookup the Language instance for.
	 * @return Language with that code, null if not found.
	 */
	public static Language getLanguageByCode( String code ) {
		if ( languageClass == null ) {
			throw new LanguagesNotConfiguredException();
		}

		return (Language) EnumUtils.getByCode( languageClass, code );
	}

	/**
	 * @return All languages possible.
	 */
	public static Language[] getLanguages() {
		if ( languageClass == null ) {
			throw new LanguagesNotConfiguredException();
		}

		return (Language[]) languageClass.getEnumConstants();
	}

    public static Class getLanguageClass() {
        return languageClass;
    }

    /**
	 * Exception thrown when the Language system has not been properly initialized.
	 * This means no LanguageConfigurator singleton has been created.
	 */
	public static final class LanguagesNotConfiguredException extends RuntimeException
	{
		private LanguagesNotConfiguredException() {
			super( "Languages have not been configured, please initialize the system by creating a LanguageConfigurator first." );
		}
	}
}
