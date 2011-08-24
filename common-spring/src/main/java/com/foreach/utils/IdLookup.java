package com.foreach.utils;

/**
 * IdLookup is an interface to facilitate finding elements in small sets of enumerated Objects,
 * usually a java Enum.
 *
 * <p>getId returns a value that can be used to retrieve the Object.
 * The actual lookup is not part of this interface,
 * because it will be in most cases implemented by either a static method or a service.</p>
 *
 * <p>Typical usecases are web requests or database persistence.</p>
 */
public interface IdLookup<I>
{
	I getId();
}
