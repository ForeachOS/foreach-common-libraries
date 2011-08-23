package com.foreach.web.editors;

import com.foreach.utils.EnumUtils;
import com.foreach.utils.IdLookup;

public abstract class IdBasedEnumPropertyEditor<I,E extends Enum<E> & IdLookup<I>>
		extends BasePropertyEditor<E>
{
	private Class<E> clazz;

	public final E[] getEnumConstants()
	{
		return clazz.getEnumConstants();
	}

	protected IdBasedEnumPropertyEditor(Class<E> clazz)
	{
		this.clazz = clazz;
	}

	private E getById( I id )
	{
		return EnumUtils.getById( clazz, id );
	}

	@Override
	public final String getAsText()
	{
		E e = getObject();
		return ( e != null ) ? unparse( e.getId()) : "";
	}

	@Override
	public final void setAsText( String idAsString )
	{
		try {
			I id = parse( idAsString );
			setObject( getById( id ) );
		} catch ( NumberFormatException nfe ) {
			setObject( null );
		}
	}

	protected abstract String unparse(I i);

	protected abstract I parse(String s);
}
