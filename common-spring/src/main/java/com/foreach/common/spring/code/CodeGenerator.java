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

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.String.format;

/**
 * Generator that creates unique codes using an underlying {@link MappedStringEncoder}.
 * Codes are first generated as random numbers that are uniformly distributed across
 * the possible number range. The numbers are subsequently encoded to strings using the encoder.
 * Use {@link CodeGenerator#forCodeLength(int)} to create a code generator with a default {@link MappedStringEncoder}.
 * <p/>
 * The length of the code as well as the number of codes generated will determine the statistical
 * probability of guessing a code.
 * <p/>
 * Note that there is no uniqueness of codes between calls to the generator.
 *
 * @author Arne Vandamme
 * @see CodeGenerator#forCodeLength(int)
 */
public class CodeGenerator
{
	private final Random random;
	private final MappedStringEncoder encoder;

	public CodeGenerator( MappedStringEncoder encoder ) {
		this.encoder = encoder;

		random = new Random( System.currentTimeMillis() );
	}

	/**
	 * Generates a number of random codes that are all the same - full - length.
	 *
	 * @param numberOfCodes Number of codes to generate.
	 * @return List of codes.
	 * @throws java.lang.IllegalArgumentException if the number of codes is too large for the configured encoder.
	 */
	public List<String> generate( int numberOfCodes ) {
		return generate( numberOfCodes, true );
	}

	/**
	 * Generates a number of random codes.
	 *
	 * @param numberOfCodes Number of codes to generate.
	 * @param padToLength   True if codes should all be the same - full -length.
	 * @return List of codes.
	 * @throws java.lang.IllegalArgumentException if the number of codes is too large for the configured encoder.
	 */
	public List<String> generate( int numberOfCodes, boolean padToLength ) {
		Assert.isTrue( numberOfCodes >= 0, () -> format("Expected numberOfCodes >= 0 but got: %s", numberOfCodes) );

		List<String> codes = new ArrayList<>( numberOfCodes );

		long maxValue = encoder.getMaxValue();

		if ( ( numberOfCodes - 1 ) > maxValue ) {
			throw new IllegalArgumentException( "Unable to generate " + numberOfCodes + " codes" );
		}

		long slice = calculateSlice( maxValue, numberOfCodes );
		int randomRange = calculateRangeForRandom( slice );

		for ( int i = 0; i < numberOfCodes; i++ ) {
			int delta = random.nextInt( randomRange );
			long number = i * slice + delta;

			codes.add( encoder.encode( number, padToLength ) );
		}

		return codes;
	}

	// Divide the maximum values in as many slices as codes are requested
	private long calculateSlice( long maxValue, int numberOfCodes ) {
		return Math.max( maxValue / ( numberOfCodes + 1 ), 1 );
	}

	// Calculate the random factor: the is the upper bound (exclusive) for the random number
	// that serves as the offset in the current slice
	private int calculateRangeForRandom( long slice ) {
		if ( slice > Integer.MAX_VALUE ) {
			return Integer.MAX_VALUE;
		}

		return Long.valueOf( slice ).intValue();
	}

	/**
	 * Create a CodeGenerator that generates random codes with a specified length.
	 *
	 * @param length Length of a code.
	 * @return CodeGenerator.
	 */
	public static CodeGenerator forCodeLength( int length ) {
		MappedStringEncoder encoder = new MappedStringEncoder( length, false );

		return new CodeGenerator( encoder );
	}
}
