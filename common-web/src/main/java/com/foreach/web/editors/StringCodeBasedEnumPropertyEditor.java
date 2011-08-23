package com.foreach.web.editors;

import com.foreach.utils.CodeLookup;


/**
 * <p>An abstract PropertyEditor for Enums implementing CodeLookup&ltString&gt;.</p>
 *
 * <p>Subclasses need only provide the actual Enum used. For example:</p>
 *
 * <pre>
 *  public enum Country implements CodeLookup&lt;String&gt;
 *
 *  ...
 *
 *  public class CountryEditor extends StringCodeBasedEnumPropertyEditor<Country>
 *  {
 *      public CountryEditor()
 *      {
 *          super( Country.class );
 *      }
 *  }
 * </pre>
 */

public class StringCodeBasedEnumPropertyEditor<E extends Enum<E> & CodeLookup<String>>
		extends CodeBasedEnumPropertyEditor<String,E>
{
	protected StringCodeBasedEnumPropertyEditor( Class<E> clazz )
	{
		super( clazz ) ;
	}

	// convert from Code type (String) to String

	@Override
	protected String render( String code )
	{
		return code;
	}

	// convert from String to the Code type (also a String)

	@Override
	protected String unRender( String s )
	{
		return s;
	}

}
