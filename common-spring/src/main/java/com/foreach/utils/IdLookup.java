package com.foreach.utils;

/**
 * IdLookup is an interface to facilitate finding elements in small sets of enumerated Objects,
 * usually a java Enum.
 * <p/>
 * getId returns a value that can be used to retrieve the Object.
 * The actual lookup is not part of this interface,
 * because it will be in most cases implemented by either a static method or a service.
 * <p/>
 * In order for this interface to be of use, getId must return unique codes across all instances
 * of a given implementation, so when
 * <pre>
 *  class Foo implements IdLookup&ltInteger&gt;
 * </pre>
 * then if
 * <pre>
 *     foo1.getId().equals(foo2.getId())
 * </pre>
 * is true, so should.
 * <pre>
 *     foo1.equals(foo2)
 * </pre>
 * <p/>
 * Typical usecases are web requests or database persistence.
 */
public interface IdLookup<I>
{
	/**
	 * Returns the unique id of type &ltI&gt; for this instance.
	 */
	I getId();
}
