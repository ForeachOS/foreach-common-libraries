package com.foreach.web.editors;

import java.beans.PropertyEditorSupport;

public abstract class BasePropertyEditor<P> extends PropertyEditorSupport
{
	private P object;

	@Override
	public final Object getValue()
	{
		return object;
	}

	@Override
	public final void setValue( Object object )
	{
		this.object = (P) object;
	}

	// typesafe getter and setter for internal use
	protected final P getObject()
	{
		return object;
	}

	protected final void setObject( P object )
	{
		this.object = object;
	}
}
