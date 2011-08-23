package com.foreach.web.editors;

import com.foreach.utils.CodeLookup;
import com.foreach.utils.EnumUtils;

/**
 * An abstract PropertyEditor for Enums implementing CodeLookup&ltS&gt;.
 *
 * Subclasses must implement the render and unrender methods
 * to convert from S to String and back.
 */
public abstract class CodeBasedEnumPropertyEditor<S,E extends Enum<E> & CodeLookup<S>>
		extends BasePropertyEditor<E>
{
	protected Class<E> clazz;

	protected CodeBasedEnumPropertyEditor( Class<E> clazz )
	{
		this.clazz = clazz;
	}

	private E getByCode( S code )
	{
		return EnumUtils.getByCode( clazz, code );
	}

	@Override
	public final String getAsText()
	{
		E e = getObject();
		return ( e != null ) ? render( e.getCode() ) : "";
	}

	@Override
	public final void setAsText( String s )
	{
		setObject( getByCode( unRender( s ) ) );
	}

	protected abstract String render(S code);

	protected abstract S unRender(String s);
}

