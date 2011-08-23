package com.foreach.utils;


public class EnumUtils extends org.apache.commons.lang.enums.EnumUtils
{
	private EnumUtils()
	{
	}

	public static <I,E extends Enum<E> & IdLookup<I>> E getById( Class<E> clazz, I id )
	{
		for ( E e : clazz.getEnumConstants() ) {
			if ( e.getId().equals(id) ) {
				return e;
			}
		}
		return null;
	}

	public static <S,E extends Enum<E> & CodeLookup<S>> E getByCode( Class<E> clazz, S code )
	{
		for ( E e : clazz.getEnumConstants() ) {
			if ( e.getCode().equals( code ) ) {
				return e;
			}
		}
		return null;
	}

}
