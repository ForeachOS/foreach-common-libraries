package com.foreach.spring.utils;

/**
 * EnumUtils contains some utility routines to find specific enums if their classes
 * implement Idlookup or CodeLookup.
 */
public final class EnumUtils
{
	private EnumUtils()
	{
	}

	/**
	 * @param clazz an Enum class implementing IdLookup&lt;I&gt;
	 * @param id    an instance of type I
	 * @return the instance e of class clazz such that e.getId().equals( id )
	 */
	public static <I, E extends Enum<E> & IdLookup<I>> E getById( Class<E> clazz, I id )
	{
		for ( E e : clazz.getEnumConstants() ) {
			if ( e.getId().equals( id ) ) {
				return e;
			}
		}
		return null;
	}

	/**
	 * @param clazz an Enum class implementing CodeLookup&lt;S&gt;
	 * @param code  an instance of type S
	 * @return the instance e of class clazz such that e.getCode().equals( code )
	 */
	public static <S, E extends Enum<E> & CodeLookup<S>> E getByCode( Class<E> clazz, S code )
	{
		for ( E e : clazz.getEnumConstants() ) {
			if ( e.getCode().equals( code ) ) {
				return e;
			}
		}
		return null;
	}
}
