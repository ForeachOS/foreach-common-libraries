package com.foreach.spring.localization;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An object that contains different versions of {@link LocalizedFields} instances providing language versioned fields.
 */
public abstract class AbstractLocalizedFieldsObject<Base extends LocalizedFields>
{
	private final Map<String, Base> fieldsByLanguageCode = new HashMap<String, Base>();
	private Map<String, Base> fieldsAsUnmodifiableMap = null;
	private Collection<Base> fieldsAsModifiableCollection = null;

	protected AbstractLocalizedFieldsObject()
	{
		createDefaultFields();
	}

	/**
	 * <p>Provides an unmodifiable map interface to access the fields by language code (String accessor).
	 * This allows easier use in things like JSP or mapping files:</p>
	 * <ul>
	 * <li>myobject.fields[nl].text <em>(JSTL/JSP)</em></li>
	 * <li>myobject.fields.nl.text <em>(MyBatis/OGNL implementations)</em></li>
	 * </ul>
	 *
	 * @return All fields in a map of language code/fields implementation.
	 */
	public final Map<String, Base> getFields()
	{
		if ( fieldsAsUnmodifiableMap == null ) {
			fieldsAsUnmodifiableMap = Collections.unmodifiableMap( fieldsByLanguageCode );
		}

		return fieldsAsUnmodifiableMap;
	}

	/**
	 * Sets all fields of the entity in one go, passing them as a collection.
	 *
	 * @param allFields Collection containing the LocalizedFields implementations.
	 */
	public final void setFieldsAsCollection( Collection<Base> allFields )
	{
		Collection<Base> current = getFieldsAsCollection();
		current.clear();

		current.addAll( allFields );
	}

	/**
	 * Provides a collection interface to all fields. Fields can be iterated or added through the collection interface.
	 *
	 * @return All fields as a collection that can be modified or iterated over.
	 */
	public final Collection<Base> getFieldsAsCollection()
	{
		if ( fieldsAsModifiableCollection == null ) {
			fieldsAsModifiableCollection = new LocalizedFieldsCollection<Base>( fieldsByLanguageCode );
		}

		return fieldsAsModifiableCollection;
	}

	/**
	 * Method to fetch fields for a given language.
	 * NOTE: If no fields for that language have been found, they will be created.
	 *
	 * @param language Language for which to fetch the fields.
	 * @return LocalizedFields for the specified language.
	 */
	public final Base getFieldsForLanguage( Language language )
	{
		Base fields;

		String languageCode = language.getCode();

		if ( fieldsByLanguageCode.containsKey( languageCode ) ) {
			fields = fieldsByLanguageCode.get( languageCode );
		}
		else {
			fields = createFields( language );

			if ( fields != null ) {
				addFields( fields );
			}
		}

		return fields;
	}

	/**
	 * Adds LocalizedFields to this entity.  The language must be set on the LocalizedFields instance.
	 * If there are already LocalizedFields linked to the same language, they will be replaced by the new instance.
	 *
	 * @param fields Specific LocalizedFields implementation.
	 */
	public final void addFields( Base fields )
	{
		if ( fields.getLanguage() == null ) {
			throw new RuntimeException( "Language is required on LocalizedFields" );
		}

		fieldsByLanguageCode.put( fields.getLanguage().getCode(), fields );
	}

	/**
	 * Removes the LocalizedFields for the specific language.
	 *
	 * @param language Language for which to remove the fields.
	 */
	public final void removeFields( Language language )
	{
		if ( language != null && fieldsByLanguageCode.containsKey( language.getCode() ) ) {
			fieldsByLanguageCode.remove( language.getCode() );
		}
	}

	/**
	 * Called after construction of this instance.  Can be used to set fields to a predefined state.
	 */
	private void createDefaultFields()
	{
		for ( Language language : LanguageConfigurator.getLanguages() ) {
			getFieldsForLanguage( language );
		}
	}

	/**
	 * Creates new LocalizedFields of the required specific implementation.  This does not add the fields to
	 * the collection for this entity.
	 *
	 * @param language Language for which to create fields.
	 * @return Specific LocalizedFields implementation.
	 */
	public abstract Base createFields( Language language );
}
