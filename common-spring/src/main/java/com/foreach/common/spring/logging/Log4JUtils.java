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
package com.foreach.common.spring.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Utility class for Log4j. This class contains utility methods such as to retrieve registered loggers.
 *
 * @version 1.0
 */
public class Log4JUtils
{
	protected Log4JUtils() {
		// protected constructor, so that this class can be extended.
	}

	/*
	 * To get a list of all registered loggers in current application.
	 * Returned list of loggers will be sorted by its name
	 *
	 * @return List of loggers
	 */
/*
	public static List<Logger> getClassLoggers() {
		List<String> loggerNames = new ArrayList<String>();
		Enumeration enumeration = LogManager.getCurrentLoggers();
		while ( enumeration.hasMoreElements() ) {
			Logger logger = (Logger) enumeration.nextElement();
			loggerNames.add( logger.getName() );
		}

		Collections.sort( loggerNames );

		List<Logger> result = new ArrayList<Logger>();

		for ( String loggerName : loggerNames ) {
			result.add( LogManager.getLogger( loggerName ) );
		}

		return result;
	}
*/
}
