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
package com.foreach.common.spring.code;


import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 */
public class TestCodeGenerator
{
	private CodeGenerator generator;

	@Test
	public void tooManyCodesRequested() {
		generator = CodeGenerator.forCodeLength( 1 );

		assertThrows( IllegalArgumentException.class, () -> generator.generate( 5000 ) );

	}

	@Test
	public void illegalNumberOfCodesRequested() {
		generator = CodeGenerator.forCodeLength( 10 );

		assertThrows( IllegalArgumentException.class,() ->generator.generate( -1 ) );
	}

	@Test
	public void generatedCodesShouldHaveSameLengthAndBeDifferent() {
		generator = CodeGenerator.forCodeLength( 10 );
		List<String> codes = generator.generate( 50000 );

		assertEquals( 50000, codes.size() );

		Set<String> handledCodes = new HashSet<>();

		for ( String code : codes ) {
			assertEquals( 10, code.length() );
			assertFalse( handledCodes.contains( code ) );

			handledCodes.add( code );
		}
	}

	@Test
	public void generateMaxPossibleCodes() {
		MappedStringEncoder encoder = new MappedStringEncoder();
		encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 3, false );

		generator = new CodeGenerator( encoder );
		List<String> codes = generator.generate( 8, true );

		assertEquals( 8, codes.size() );
		assertTrue( codes.contains( "AAA" ) );
		assertTrue( codes.contains( "AAB" ) );
		assertTrue( codes.contains( "ABA" ) );
		assertTrue( codes.contains( "ABB" ) );
		assertTrue( codes.contains( "BAA" ) );
		assertTrue( codes.contains( "BAB" ) );
		assertTrue( codes.contains( "BBA" ) );
		assertTrue( codes.contains( "BBB" ) );

		codes = generator.generate( 8, false );

		assertEquals( 8, codes.size() );
		assertTrue( codes.contains( "A" ) );
		assertTrue( codes.contains( "B" ) );
		assertTrue( codes.contains( "BA" ) );
		assertTrue( codes.contains( "BB" ) );
		assertTrue( codes.contains( "BAA" ) );
		assertTrue( codes.contains( "BAB" ) );
		assertTrue( codes.contains( "BBA" ) );
		assertTrue( codes.contains( "BBB" ) );
	}

	@Test
	public void generateSingleCode() {
		MappedStringEncoder encoder = new MappedStringEncoder();
		encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 10, false );

		generator = new CodeGenerator( encoder );
		List<String> codes = generator.generate( 1, true );

		assertEquals( 1, codes.size() );
	}
}
