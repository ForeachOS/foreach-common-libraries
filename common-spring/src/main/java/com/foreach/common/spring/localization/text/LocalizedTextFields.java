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
package com.foreach.common.spring.localization.text;

import com.foreach.common.spring.localization.BaseLocalizedFields;
import com.foreach.common.spring.localization.Language;
import org.apache.commons.lang3.StringUtils;

public final class LocalizedTextFields extends BaseLocalizedFields
{
	private String text;

	/**
	 * For deserialization.
	 */
	protected LocalizedTextFields() {
		super( null );
	}

	public LocalizedTextFields( Language language ) {
		super( language );
	}

	public String getText() {
		return text;
	}

	public void setText( String text ) {
		this.text = text;
	}

	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		LocalizedTextFields fields = (LocalizedTextFields) o;

		if ( this.getLanguage() != fields.getLanguage() ) {
			return false;
		}

		String thisText = StringUtils.defaultIfBlank( text, "" );
		String thatText = StringUtils.defaultIfBlank( fields.text, "" );

		return StringUtils.equals( thisText, thatText );
	}

	public int hashCode() {
		int result = getLanguage() != null ? getLanguage().hashCode() : 0;
		result = 31 * result + ( text != null ? text.hashCode() : 0 );
		return result;
	}
}
