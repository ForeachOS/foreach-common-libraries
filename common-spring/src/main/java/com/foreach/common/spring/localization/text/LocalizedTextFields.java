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
