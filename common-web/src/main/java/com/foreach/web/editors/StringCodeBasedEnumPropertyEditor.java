package com.foreach.web.editors;

import com.foreach.utils.CodeLookup;
import com.foreach.utils.EnumUtils;

public class StringCodeBasedEnumPropertyEditor<E extends Enum<E> & CodeLookup<String>>
		extends BasePropertyEditor<E>
{
	private Class<E> clazz;

	protected StringCodeBasedEnumPropertyEditor( Class<E> clazz )
	{
		this.clazz = clazz;
	}

	private E getByCode( String code )
	{
		return EnumUtils.getByCode( clazz, code );
	}

	@Override
	public final String getAsText()
	{
		E e = getObject();
		return ( e != null ) ? e.getCode() : "";
	}

	@Override
	public final void setAsText( String code )
	{
		setObject( getByCode( code ) );
	}
}
