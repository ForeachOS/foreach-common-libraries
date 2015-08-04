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
