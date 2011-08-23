package com.foreach.web.editors;

import com.foreach.utils.IdLookup;

public class LongIdBasedEnumPropertyEditor <E extends Enum<E> & IdLookup<Long>>

		extends IdBasedEnumPropertyEditor<Long,E>
{
	protected LongIdBasedEnumPropertyEditor(Class<E> clazz)
	{
		super(clazz);
	}

	protected final String unparse(Long i)
	{
		return Long.toString( i, 10 );
	}

	protected final Long parse(String s)
	{
		return Long.parseLong( s, 10 );
	}
}
