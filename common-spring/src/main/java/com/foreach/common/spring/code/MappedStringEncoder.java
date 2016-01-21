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

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * Encodes a number to a string according to an encoding map.
 * The encoding matrix determines the maximum length of the resulting string
 * as well as the largest number that can be encoded.
 * <p/>
 * An encoding matrix can be assymetrical, but every position must have at least one possible value,
 * and all values must be unique per position.
 * <p/>
 * Encoding is reversable: the same encoder can also decode the code back into
 * the numerical value.
 * <p/>
 * A MappedStringEncoder can optionally support negative values.  A separate position
 * in the code will be sacrificed to encode the sign, which means the maximum encodable
 * value will be impacted.  The sign itself can be represented by any character.
 *
 * @author Arne Vandamme
 */
public class MappedStringEncoder
{
	public static final int UNSIGNED = -1;

	private static final char[] DEFAULT_ENCODING_VALUES = "J3Q24EATY9U8PZDFG7H6RKMWXCVBN".toCharArray();

	private int signIndex = UNSIGNED;

	private char[][] encodingMatrix = new char[0][0];
	private char[][] unsignedEncodingMatrix = new char[0][0];

	private BigInteger maxValue;
	private BigInteger[] unitValues = new BigInteger[0];

	/**
	 * Creates a blank encoder that is unable to encode or decode any values.
	 * A valid encoding matrix needs to be set using {@link #setEncodingMatrix(char[][])}.
	 */
	public MappedStringEncoder() {
	}

	/**
	 * Creates a default encoder with a symmetrical encoding matrix.
	 *
	 * @param encodedStringLength   Length of a generated - fully padded - code.
	 * @param supportNegativeValues True if a position should be sacrificed to encode the sign.
	 */
	public MappedStringEncoder( int encodedStringLength, boolean supportNegativeValues ) {
		buildEncodingMatrix( DEFAULT_ENCODING_VALUES, encodedStringLength, true,
		                     supportNegativeValues ? 0 : UNSIGNED );
	}

	/**
	 * Creates a default encoder with a symmetrical encoding matrix.
	 *
	 * @param encodedStringLength Length of a generated - fully padded - code.
	 * @param signIndex           Index of the position where the sign value should be.
	 */
	public MappedStringEncoder( int encodedStringLength, int signIndex ) {
		buildEncodingMatrix( DEFAULT_ENCODING_VALUES, encodedStringLength, true, signIndex );
	}

	/**
	 * Sets the encoding matrix to use for encoding/decoding unsigned values.
	 *
	 * @param encodingMatrix Character matrix to use for encoding/decoding.
	 */
	public void setEncodingMatrix( char[][] encodingMatrix ) {
		setEncodingMatrix( encodingMatrix, UNSIGNED );
	}

	/**
	 * Sets the encoding matrix to use. If a sign index is specified, that position should contain an encoding array with
	 * at least 2 values: the first being the positive sign and the second the negative.  If more values are specified,
	 * these will simply ignored.  The matrix must also be at least 2 positions as one position is required for the sign.
	 * <p/>
	 * Specifying a negative sign index position will result in only positive values being supported.
	 *
	 * @param encodingMatrix Character matrix to use for encoding/decoding.
	 * @param signIndex      Index of the position where the sign value should be.
	 */
	public void setEncodingMatrix( char[][] encodingMatrix, int signIndex ) {
		Set<Character> found = new HashSet<>();
		for ( char[] anEncodingMatrix : encodingMatrix ) {
			if ( anEncodingMatrix == null || anEncodingMatrix.length < 1 ) {
				throw new IllegalArgumentException(
						"An encoding matrix needs at least one character on every position" );
			}

			for ( char value : anEncodingMatrix ) {
				if ( found.contains( value ) ) {
					throw new IllegalArgumentException( "All possible values for a given position must be unique." );
				}
				found.add( value );
			}

			found.clear();
		}

		if ( signIndex != UNSIGNED ) {
			if ( encodingMatrix.length < 2 ) {
				throw new IllegalArgumentException(
						"A sign index position is only supported in an encoding matrix with a minimum of 2 positions" );
			}

			if ( signIndex < 0 || signIndex >= encodingMatrix.length || encodingMatrix[signIndex].length < 2 ) {
				throw new IllegalArgumentException( "A sign index position needs at least 2 values on that position" );
			}
		}

		this.encodingMatrix = ArrayUtils.clone( encodingMatrix );
		this.signIndex = signIndex;

		BigInteger total = BigInteger.ONE;

		unsignedEncodingMatrix = new char[encodingMatrix.length][];
		System.arraycopy( encodingMatrix, 0, unsignedEncodingMatrix, 0, encodingMatrix.length );

		if ( signIndex != UNSIGNED ) {
			unsignedEncodingMatrix[signIndex] = new char[] { 0 };
		}

		unitValues = new BigInteger[unsignedEncodingMatrix.length];

		for ( int i = unsignedEncodingMatrix.length - 1; i >= 0; i-- ) {
			unitValues[i] = total;
			total = total.add( total.multiply( BigInteger.valueOf( unsignedEncodingMatrix[i].length - 1 ) ) );
		}

		maxValue = total.subtract( BigInteger.ONE );
	}

	/**
	 * Builds a symmetrical encoding matrix with the specified parameters.  The matrix only supports
	 * unsigned values and depending on the shift parameter, the column values will be shifted for
	 * every position. Column values of ABC would result in zero being encoded as AAA (unshifted) or ABC (shifted).
	 * <p/>
	 * The same parameters will always result in the same encoding matrix being generated.
	 *
	 * @param columnValues      The possible values for a single position.
	 * @param numberOfColumns   Number of positions in the code - also the code length.
	 * @param shiftColumnValues True if column values should be shifted for every position.
	 */
	public void buildEncodingMatrix( char[] columnValues, int numberOfColumns, boolean shiftColumnValues ) {
		buildEncodingMatrix( columnValues, numberOfColumns, shiftColumnValues, UNSIGNED );
	}

	/**
	 * Builds a symmetrical encoding matrix with the specified parameters.  Depending on the sign parameter
	 * the matrix will support signed values and depending on the shift parameter, the column values will be shifted for
	 * every position. Column values of ABC would result in zero being encoded as AAA (unshifted) or ABC (shifted).
	 * <p/>
	 * The same parameters will always result in the same encoding matrix being generated.
	 *
	 * @param columnValues      The possible values for a single position.
	 * @param numberOfColumns   Number of positions in the code - also the code length.
	 * @param shiftColumnValues True if column values should be shifted for every position.
	 * @param signColumnIndex   Index of the position where the sign value should be.
	 */
	public void buildEncodingMatrix( char[] columnValues,
	                                 int numberOfColumns,
	                                 boolean shiftColumnValues,
	                                 int signColumnIndex ) {
		char[][] matrix = new char[numberOfColumns][];

		char[] shiftingValues = ArrayUtils.clone( columnValues );

		for ( int i = 0; i < numberOfColumns; i++ ) {
			matrix[i] = shiftingValues;
			shiftingValues = shiftColumnValues ? shift( shiftingValues ) : ArrayUtils.clone( columnValues );
		}

		setEncodingMatrix( matrix, signColumnIndex );
	}

	private char[] shift( char[] original ) {
		char[] shifted = new char[original.length];

		System.arraycopy( original, 1, shifted, 0, original.length - 1 );

		shifted[original.length - 1] = original[0];

		return shifted;
	}

	/**
	 * Encodes a number to a code string.
	 *
	 * @param number Number to encode.
	 * @param pad    True if resulting code should be padded to the full length.
	 * @return Code representing the number.
	 * @throws java.lang.IllegalArgumentException in case the number cannot be encoded
	 */
	public String encode( long number, boolean pad ) {
		char[] code = new char[getCodeLength()];
		BigInteger value = BigInteger.valueOf( number );

		if ( value.compareTo( BigInteger.valueOf( getMinValue() ) ) < 0 ) {
			throw new IllegalArgumentException(
					"Unable to encode value " + value + " as it smaller than the minimum value" );
		}

		if ( value.compareTo( maxValue ) > 0 ) {
			throw new IllegalArgumentException( "Unable to encode value " + value + " as it is too large" );
		}

		boolean negative = value.compareTo( BigInteger.ZERO ) < 0;

		if ( negative ) {
			value = value.abs();
		}

		boolean charactersSet = false;
		for ( int i = 0; i < unsignedEncodingMatrix.length; i++ ) {
			BigInteger[] divideAndRemainder = value.divideAndRemainder( unitValues[i] );
			BigInteger divide = divideAndRemainder[0];
			BigInteger remainder = divideAndRemainder[1];

			if ( divide.compareTo(
					BigInteger.ZERO ) > 0 || charactersSet || i == ( unsignedEncodingMatrix.length - 1 ) ) {
				charactersSet = true;
				code[i] = unsignedEncodingMatrix[i][divide.intValue()];
			}

			value = remainder;
		}

		if ( signIndex != UNSIGNED && negative ) {
			code[signIndex] = encodingMatrix[signIndex][1];

			// Make sure positions between the sign and first values are filled
			for ( int i = signIndex + 1; i < code.length; i++ ) {
				if ( code[i] == 0 ) {
					code[i] = unsignedEncodingMatrix[i][0];
				}
			}
		}

		if ( pad ) {
			pad( code );
		}

		return new String( code ).trim();
	}

	private void pad( char[] code ) {
		for ( int i = 0; i < code.length; i++ ) {
			if ( code[i] == 0 ) {
				if ( i == signIndex ) {
					code[i] = encodingMatrix[i][0];
				}
				else {
					code[i] = unsignedEncodingMatrix[i][0];
				}
			}
		}
	}

	/**
	 * Decode a code string back to a number.
	 *
	 * @param encoded Code string.
	 * @return Number represented by the code.
	 * @throws java.lang.IllegalArgumentException if the code cannot be decoded with this encoder instance.
	 */
	public long decode( String encoded ) {
		char[] code = parseCode( encoded );

		boolean negative = false;

		if ( isNegativeValuesSupported() ) {
			negative = ArrayUtils.indexOf( encodingMatrix[signIndex], code[signIndex] ) > 0;
			code[signIndex] = 0;
		}

		BigInteger value = BigInteger.ZERO;

		for ( int i = unsignedEncodingMatrix.length - 1; i >= 0; i-- ) {
			int index = ArrayUtils.indexOf( unsignedEncodingMatrix[i], code[i] );

			if ( index < 0 ) {
				throw new IllegalArgumentException( "Illegal code value " + code[i] + " found on position " + i );
			}

			value = value.add( unitValues[i].multiply( BigInteger.valueOf( index ) ) );
		}

		return negative ? value.negate().longValue() : value.longValue();
	}

	private char[] parseCode( String encoded ) {
		char[] code = new char[getCodeLength()];

		if ( encoded.length() > code.length ) {
			throw new IllegalArgumentException( "Encoded value is too long: " + encoded );
		}

		System.arraycopy( encoded.toCharArray(), 0, code, ( code.length - encoded.length() ), encoded.length() );

		pad( code );

		return code;
	}

	/**
	 * @return Full length of a code.
	 */
	public int getCodeLength() {
		return encodingMatrix.length;
	}

	/**
	 * @return True if negative values are supported, if so a sign position is defined.
	 */
	public boolean isNegativeValuesSupported() {
		return signIndex != UNSIGNED;
	}

	/**
	 * @return Largest possible value that can be encoded.
	 */
	public long getMaxValue() {
		if ( encodingMatrix.length > 0 ) {
			if ( BigInteger.valueOf( Long.MAX_VALUE ).compareTo( maxValue ) > 0 ) {
				return maxValue.longValue();
			}
			else {
				return Long.MAX_VALUE;
			}
		}

		return -1;
	}

	/**
	 * @return Smallest possible value that can be encoded.
	 */
	public long getMinValue() {
		return isNegativeValuesSupported() ? -1 * getMaxValue() : 0;
	}

	/**
	 * Will create the shortest possible encoder that can encode the maximum value specified.
	 * This will use a symmetrical encoding matrix.
	 * <p/>
	 * In case of a negative value supporting encoding, the maximum value should be the absolute
	 * upper bound.
	 *
	 * @param maxValue              Maximum value that needs to be encoded.
	 * @param supportNegativeValues True if negative values need to be encodable as well.
	 * @return Encoder instance.
	 */
	public static MappedStringEncoder forMaximumValue( long maxValue, boolean supportNegativeValues ) {
		Assert.isTrue( maxValue >= 0, "Maximum value must not be a negative number" );

		int length = 1;

		BigInteger maxRequired = BigInteger.valueOf( maxValue );
		BigInteger maxSupported = BigInteger.valueOf( DEFAULT_ENCODING_VALUES.length );

		while ( maxSupported.compareTo( maxRequired ) < 0 ) {
			length++;
			maxSupported =
					maxSupported.add( maxSupported.multiply( BigInteger.valueOf( DEFAULT_ENCODING_VALUES.length ) ) );
		}

		if ( supportNegativeValues ) {
			length++;
		}

		return new MappedStringEncoder( length, supportNegativeValues );
	}
}
