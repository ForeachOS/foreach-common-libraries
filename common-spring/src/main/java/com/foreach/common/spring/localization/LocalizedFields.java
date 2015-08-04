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
 * LocalizedFields interface is used to link a collection of properties to a specific language.
 * See also {@link AbstractLocalizedFieldsObject} for a base entity that uses the LocalizedFields interface,
 * and {@link BaseLocalizedFields} for a base implementation of this interface.
 */
public interface LocalizedFields
{
	/**
	 * @return Language these fields are linked to.
	 */
	Language getLanguage();
}

