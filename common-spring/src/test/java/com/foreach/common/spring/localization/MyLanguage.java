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

import java.util.Locale;

public enum MyLanguage implements Language
{
	EN( "en", "English", Locale.ENGLISH ),
	FR( "fr", "Français", Locale.FRENCH );

	private String code, name;
	private Locale locale;

	MyLanguage( String code, String name, Locale locale ) {
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
