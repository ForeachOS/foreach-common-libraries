package com.foreach.spring.localization;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
	An object that contains different versions of <Base> objects dependent on language
 */
public abstract class AbstractLocalizedFieldsObject<Base extends LocalizedFields>
{
	private Map<Language, Base> fieldsMap = new HashMap<Language, Base>();
	private Collection<Base> fieldsCollection = new LocalizedFieldsCollection();

	protected AbstractLocalizedFieldsObject()
	{
		createDefaultFields();
	}

	public final Collection<Base> getFieldsAsCollection()
	{
		return fieldsCollection;
	}

	public final void setFields( Map<Language, Base> fieldsMap )
	{
		this.fieldsMap = fieldsMap;
	}

	public final Map<Language, Base> getFields()
	{
		return fieldsMap;
	}

	public final Base getFields( Language language )
	{
		Base fields;

		if ( fieldsMap.containsKey( language ) ) {
			fields = fieldsMap.get( language );
		}
		else {
			fields = createFields( language );

			fieldsMap.put( language, fields );
		}

		return fields;
	}

	protected abstract void createDefaultFields();

	public abstract Base createFields( Language language );

	/**
	 * Helper class for allowing fields to be set as general collection instead of map.
	 */
	private class LocalizedFieldsCollection implements Collection<Base>
	{
		public int size()
		{
			return fieldsMap.size();
		}

		public boolean isEmpty()
		{
			return fieldsMap.isEmpty();
		}

		public boolean contains( Object o )
		{
			return fieldsMap.containsValue( o );
		}

		public Iterator<Base> iterator()
		{
			return fieldsMap.values().iterator();
		}

		public Object[] toArray()
		{
			return fieldsMap.values().toArray();
		}

		public <T> T[] toArray( T[] a )
		{
			return fieldsMap.values().toArray( a );
		}

		public boolean add( Base base )
		{
			if ( base.getLanguage() != null ) {
				fieldsMap.put( base.getLanguage(), base );
				return true;
			}
			return false;
		}

		public boolean remove( Object o )
		{
			boolean found = false;
			Language language = null;

			for ( Map.Entry<Language, Base> entry : fieldsMap.entrySet() ) {
				if ( entry.getValue().equals( o ) ) {
					language = entry.getKey();
					found = true;
				}
			}

			Base base = found ? fieldsMap.remove( language ) : null;

			return base != null;
		}

		public boolean containsAll( Collection<?> c )
		{
			return fieldsMap.entrySet().containsAll( c );
		}

		public boolean addAll( Collection<? extends Base> c )
		{
			for ( Base b : c ) {
				add( b );
			}

			return true;
		}

		public boolean removeAll( Collection<?> c )
		{
			boolean success = true;

			for ( Object o : c ) {
				if ( !remove( o ) ) {
					success = false;
				}
			}

			return success;
		}

		public boolean retainAll( Collection<?> c )
		{
			return fieldsMap.entrySet().retainAll( c );
		}

		public void clear()
		{
			fieldsMap.clear();
		}
	}
}