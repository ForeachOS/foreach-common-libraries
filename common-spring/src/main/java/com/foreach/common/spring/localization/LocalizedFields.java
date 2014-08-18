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

