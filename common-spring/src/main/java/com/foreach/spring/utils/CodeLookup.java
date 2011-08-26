package com.foreach.spring.utils;

/**
 * CodeLookup is an interface to facilitate finding elements in small sets of enumerated Objects,
 * usually a java Enum.
 * <p/>
 * getCode returns a value that can be used to retrieve the Object.
 * The actual lookup is not part of this interface,
 * because it will be in most cases implemented by either a static method or a service.
 * <p/>
 * In order for this interface to be of use, getCode must return unique codes across all instances
 * of a given implementation, so when
 * <pre>
 *  class Foo implements CodeLookup&ltInteger&gt;
 * </pre>
 * then if
 * <pre>
 *     foo1.getCode().equals(foo2.getCode())
 * </pre>
 * is true, so should.
 * <pre>
 *     foo1.equals(foo2)
 * </pre>
 * <p/>
 * Typical usecases are web requests or database persistence.
 */
public interface CodeLookup<S>
{
	/**
	 * Returns the unique code of type &ltS&gt; for this instance.
	 */
	S getCode();
}
