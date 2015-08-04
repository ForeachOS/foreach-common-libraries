/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.common.spring.enums;

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
