package com.foreach.mybatis.util;


public class EnumUtils extends org.apache.commons.lang.enums.EnumUtils
{
	private EnumUtils()
	{
	}

	public static <I,E extends Enum<E> & IdLookup<I>> E getById( Class<E> clazz, I id )
	{
		for ( E e : clazz.getEnumConstants() ) {
			if ( e.getId() == id ) {
				return e;
			}
		}
		return null;
	}

	public static <E extends Enum<E> & CodeLookup> E getByCode( Class<E> clazz, String code )
	{
		for ( E e : clazz.getEnumConstants() ) {
			if ( e.getCode().equalsIgnoreCase( code ) ) {
				return e;
			}
		}
		return null;
	}

}
