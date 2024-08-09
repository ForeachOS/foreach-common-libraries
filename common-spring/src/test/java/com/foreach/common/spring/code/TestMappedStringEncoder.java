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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 */
public class TestMappedStringEncoder
{
	private MappedStringEncoder encoder;

	@BeforeEach
	public void createEncoders() {
		encoder = new MappedStringEncoder();
	}

	@Test
	public void allPositionsMustHaveAtLeastOneValue() {
		assertThrows( IllegalArgumentException.class, () -> encoder.setEncodingMatrix(
				new char[][] {
						new char[] { 'A', 'B' },
						new char[0],
						new char[] { 'B' },
						}
		) );

	}

	@Test
	public void nullEncodingMatrixRow() {
		assertThrows( IllegalArgumentException.class, () -> encoder.setEncodingMatrix(
				new char[][] {
						new char[] { 'A', 'B' },
						null,
						new char[] { 'B', 'C' },
						}
		) );
	}

	@Test
	public void duplicateValuesOnTheSamePositionAreNotAllowed() {
		assertThrows( IllegalArgumentException.class, () -> encoder.setEncodingMatrix(
				new char[][] {
						new char[] { 'A', 'B' },
						new char[] { 'B', 'B' },
						new char[] { 'B', 'C' },
						}
		) );
	}

	@Test
	public void negativeSignIndex() {
		assertThrows( IllegalArgumentException.class, () ->
				encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 2, false, -2 ) );
	}

	@Test
	public void signIndexOutOfBounds() {
		assertThrows( IllegalArgumentException.class, () ->
				encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 2, false, 2 ) );
	}

	@Test
	public void negativeValuesCanOnlyBeSupportedOnAMatrixWithMinimumTwoPositions() {
		assertThrows( IllegalArgumentException.class, () -> encoder.setEncodingMatrix(
				new char[][] {
						new char[] { 'A', 'B' }
				},
				0
		) );
	}

	@Test
	public void encodingTooLargeValue() {
		encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 3, false );
		assertThrows( IllegalArgumentException.class, () -> encoder.encode( 100, false ) );
	}

	@Test
	public void encodingTooSmallValue() {
		encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 3, true );
		assertThrows( IllegalArgumentException.class, () -> encoder.encode( -100, false ) );
	}

	@Test
	public void encodingNegativeValueOnUnsignedEncoder() {
		encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 3, false );
		assertThrows( IllegalArgumentException.class, () -> encoder.encode( -1, false ) );
	}

	@Test
	public void decodingTooLongCode() {
		encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 3, false );
		assertThrows( IllegalArgumentException.class, () -> encoder.decode( "AAAA" ) );
	}

	@Test
	public void decodingInvalidCode() {
		encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 3, false );
		assertThrows( IllegalArgumentException.class, () -> encoder.decode( "ABC" ) );
	}

	@Test
	public void zeroSizeEncoders() {
		assertEquals( -1, encoder.getMaxValue() );
		assertEquals( 0, encoder.getCodeLength() );

		encoder.buildEncodingMatrix( new char[] { 'A' }, 16, false );
		assertEquals( 0, encoder.getMaxValue() );
		assertEquals( 16, encoder.getCodeLength() );

		encoder.buildEncodingMatrix( new char[] { 'B' }, 32, false );
		assertEquals( 0, encoder.getMaxValue() );
		assertEquals( 32, encoder.getCodeLength() );

		encoder.setEncodingMatrix( new char[][] {
				new char[] { 'A' },
				new char[] { 'B' },
				new char[] { 'C' }
		} );

		assertEquals( 0, encoder.getMaxValue() );
		assertEquals( 3, encoder.getCodeLength() );
	}

	@Test
	public void maxValueCalculationForSymetricalMatrix() {
		encoder.buildEncodingMatrix( "0123456789".toCharArray(), 1, false );
		assertEquals( 9, encoder.getMaxValue() );   // 10 possible values
		assertEquals( 1, encoder.getCodeLength() );

		encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 2, false );
		assertEquals( 3, encoder.getMaxValue() );   // 4 possible values
		assertEquals( 2, encoder.getCodeLength() );

		encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 3, false );
		assertEquals( 7, encoder.getMaxValue() );   // 8 possible values
		assertEquals( 3, encoder.getCodeLength() );

		encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 4, false );
		assertEquals( 15, encoder.getMaxValue() );  // 16 possible values
		assertEquals( 4, encoder.getCodeLength() );
	}

	@Test
	public void maxValueCalculationForAsymmetricalMatrix() {
		encoder.setEncodingMatrix( new char[][] {
				new char[] { 'A' },
				new char[] { 'B', 'C' }
		} );
		assertFalse( encoder.isNegativeValuesSupported() );
		assertEquals( 0, encoder.getMinValue() );
		assertEquals( 1, encoder.getMaxValue() );   // 2 possible values
		assertEquals( 2, encoder.getCodeLength() );

		encoder.setEncodingMatrix( new char[][] {
				new char[] { 'A' },
				new char[] { 'A', 'B', 'C' }
		} );
		assertEquals( 2, encoder.getMaxValue() );   // 3 possible values
		assertEquals( 2, encoder.getCodeLength() );

		encoder.setEncodingMatrix( new char[][] {
				new char[] { 'A', },
				new char[] { 'A', 'B', 'C' },
				new char[] { 'D', 'E' }
		} );
		assertEquals( 5, encoder.getMaxValue() );   // 6 possible values
		assertEquals( 3, encoder.getCodeLength() );
	}

	@Test
	public void encodeAndDecodeSimpleSeries() {
		encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 3, false );

		assertEquals( "AAA", encoder.encode( 0, true ) );
		assertEquals( "AAB", encoder.encode( 1, true ) );
		assertEquals( "ABA", encoder.encode( 2, true ) );
		assertEquals( "ABB", encoder.encode( 3, true ) );
		assertEquals( "BAA", encoder.encode( 4, true ) );
		assertEquals( "BAB", encoder.encode( 5, true ) );
		assertEquals( "BBA", encoder.encode( 6, true ) );
		assertEquals( "BBB", encoder.encode( 7, true ) );

		assertEquals( 0, encoder.decode( "AAA" ) );
		assertEquals( 1, encoder.decode( "AAB" ) );
		assertEquals( 2, encoder.decode( "ABA" ) );
		assertEquals( 3, encoder.decode( "ABB" ) );
		assertEquals( 4, encoder.decode( "BAA" ) );
		assertEquals( 5, encoder.decode( "BAB" ) );
		assertEquals( 6, encoder.decode( "BBA" ) );
		assertEquals( 7, encoder.decode( "BBB" ) );
	}

	@Test
	public void negativeSupportingSeries() {
		encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 3, false, 0 );

		assertTrue( encoder.isNegativeValuesSupported() );
		assertEquals( -3, encoder.getMinValue() );
		assertEquals( 3, encoder.getMaxValue() );

		assertEquals( "AAA", encoder.encode( 0, true ) );
		assertEquals( "AAB", encoder.encode( 1, true ) );
		assertEquals( "ABA", encoder.encode( 2, true ) );
		assertEquals( "ABB", encoder.encode( 3, true ) );
		assertEquals( "BAB", encoder.encode( -1, true ) );
		assertEquals( "BBA", encoder.encode( -2, true ) );
		assertEquals( "BBB", encoder.encode( -3, true ) );

		encoder.buildEncodingMatrix( new char[] { 'A', 'B' }, 3, false, 1 );

		assertTrue( encoder.isNegativeValuesSupported() );
		assertEquals( -3, encoder.getMinValue() );
		assertEquals( 3, encoder.getMaxValue() );

		assertEquals( "AAA", encoder.encode( 0, true ) );
		assertEquals( "AAB", encoder.encode( 1, true ) );
		assertEquals( "BAA", encoder.encode( 2, true ) );
		assertEquals( "BAB", encoder.encode( 3, true ) );
		assertEquals( "ABB", encoder.encode( -1, true ) );
		assertEquals( "BBA", encoder.encode( -2, true ) );
		assertEquals( "BBB", encoder.encode( -3, true ) );
	}

	@Test
	public void simpleDecodeAndEncode() {
		encoder.setEncodingMatrix( new char[][] {
				new char[] { 'A' },
				new char[] { 'B', 'C' }
		} );

		assertEquals( "B", encoder.encode( 0, false ) );
		assertEquals( "C", encoder.encode( 1, false ) );
		assertEquals( "AB", encoder.encode( 0, true ) );
		assertEquals( "AC", encoder.encode( 1, true ) );

		encoder.setEncodingMatrix( new char[][] {
				new char[] { 'D', 'E' },
				new char[] { 'A' },
				new char[] { 'B', 'C' }
		} );

		assertEquals( "B", encoder.encode( 0, false ) );
		assertEquals( "DAB", encoder.encode( 0, true ) );
		assertEquals( 0, encoder.decode( "B" ) );
		assertEquals( 0, encoder.decode( "AB" ) );
		assertEquals( 0, encoder.decode( "DAB" ) );

		assertEquals( "C", encoder.encode( 1, false ) );
		assertEquals( "DAC", encoder.encode( 1, true ) );
		assertEquals( 1, encoder.decode( "C" ) );
		assertEquals( 1, encoder.decode( "AC" ) );
		assertEquals( 1, encoder.decode( "DAC" ) );

		assertEquals( "EAB", encoder.encode( 2, false ) );
		assertEquals( "EAB", encoder.encode( 2, true ) );
		assertEquals( 2, encoder.decode( "EAB" ) );

		assertEquals( "EAC", encoder.encode( 3, false ) );
		assertEquals( "EAC", encoder.encode( 3, true ) );
		assertEquals( 3, encoder.decode( "EAC" ) );
	}

	@Test
	public void complexEncodeAndDecode() {
		encoder = new MappedStringEncoder( 14, true );

		String padded = encoder.encode( 12568765965L, true );
		String nonPadded = encoder.encode( 12568765965L, false );

		assertTrue( padded.length() > nonPadded.length() );

		assertEquals( 12568765965L, encoder.decode( padded ) );
		assertEquals( 12568765965L, encoder.decode( nonPadded ) );

		padded = encoder.encode( -123456789, true );
		nonPadded = encoder.encode( -123456789, false );

		// Since the sign is the very first position, negative number will always be full length
		assertEquals( padded, nonPadded );

		assertEquals( -123456789L, encoder.decode( padded ) );
		assertEquals( -123456789L, encoder.decode( nonPadded ) );
	}

	@Test
	public void encoderWithSignPosition() {
		encoder = new MappedStringEncoder( 3, 1 );

		assertTrue( encoder.isNegativeValuesSupported() );
		assertEquals( -840, encoder.getMinValue() );
		assertEquals( 840, encoder.getMaxValue() );

		assertEquals( "J3Q", encoder.encode( 0, true ) );
		assertEquals( "J32", encoder.encode( 1, true ) );
		assertEquals( "JQ2", encoder.encode( -1, true ) );
	}

	@Test
	public void encoderForMaxValue() {
		encoder = MappedStringEncoder.forMaximumValue( 7, false );
		assertEquals( 1, encoder.getCodeLength() );

		encoder = MappedStringEncoder.forMaximumValue( 7, true );
		assertEquals( 2, encoder.getCodeLength() );

		encoder = MappedStringEncoder.forMaximumValue( 50, false );
		assertEquals( 2, encoder.getCodeLength() );

		encoder = MappedStringEncoder.forMaximumValue( 50, true );
		assertEquals( 3, encoder.getCodeLength() );

		encoder = MappedStringEncoder.forMaximumValue( Integer.MAX_VALUE, true );
		assertEquals( 8, encoder.getCodeLength() );

		encoder = MappedStringEncoder.forMaximumValue( Long.MAX_VALUE, true );
		assertEquals( 14, encoder.getCodeLength() );
	}

	@Test
	public void illegalEncoderForNegativeMaxValue() {
		assertThrows( IllegalArgumentException.class, () -> MappedStringEncoder.forMaximumValue( -1, true ) );
	}
}
