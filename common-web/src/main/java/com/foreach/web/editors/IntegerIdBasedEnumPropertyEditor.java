package com.foreach.web.editors;

import com.foreach.utils.IdLookup;


/**
 * <p>An abstract PropertyEditor for Enums that implement IdLookup&ltInteger&gt;</p>.
 *
 * <p>Subclasses need only provide the actual Enum used. For example:</p>
 *
 * <pre>
 *  public class PaymentTypeEditor extends IntegerIdBasedEnumPropertyEditor<Country>
 *  {
 *      public PaymentTypeEditor()
 *      {
 *          super( PaymentType.class );
 *      }
 *  }
 * </pre>
 */

public class IntegerIdBasedEnumPropertyEditor<E extends Enum<E> & IdLookup<Integer>>

		extends IdBasedEnumPropertyEditor<Integer,E>
{
	protected IntegerIdBasedEnumPropertyEditor( Class<E> clazz )
	{
		super( clazz );
	}

	protected final String unparse( Integer i )
	{
		return Integer.toString( i, 10 );
	}

	protected final Integer parse( String s )
	{
		return Integer.parseInt( s, 10 );
	}
}
