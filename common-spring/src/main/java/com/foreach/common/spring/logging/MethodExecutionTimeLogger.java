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

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The MethodExecutionTimeLogger class contains utility method which can be used to log the method execution time.
 * To use, inherit this class in your actual AspectJ class where you define advice method and point-cut methods.
 * The advice method of your AspectJ class should use/call method {@link #proceedAndLogExecutionTime} for logging
 * the method execution time in log files.
 * </p>
 * <p/>
 * <p>To use this class, perform following steps
 * <ul>
 * <li>Inherit this class in your actual AspectJ class where you define advice and point-cut methods</li>
 * <li>Define point-cut methods for which you wants method execution time to be logged</li>
 * <li>In your advice method call the method {@link #proceedAndLogExecutionTime}. This will log the method execution time</li>
 * <li>All methods with duration of > 75 milliseconds will be logged by default.
 * You can change this minimum duration time by method MethodExecutionTimeLogger.setMinimumDuration</li>
 * </ul>
 * </p>
 *
 * @version 1.0
 */
public class MethodExecutionTimeLogger
{
	private Logger logger = LoggerFactory.getLogger( getClass() );

	private int minimumDuration = 75;

	public MethodExecutionTimeLogger() {
	}

	/**
	 * Specify your own logger where you want the method duration time to be logged
	 *
	 * @param log
	 */
	protected final void setLogger( Logger log ) {
		this.logger = log;
	}

	/**
	 * Get the logger
	 *
	 * @return Logger
	 */
	protected final Logger getLogger() {
		return this.logger;
	}

	/**
	 * Specify the minimum duration time of methods in milliseconds. Default is 75 milliseconds.
	 * Methods whose processing time is greater then specified minimum duration time will be logged.
	 *
	 * @param time minimum duration time of methods in milliseconds (exclusive)
	 */
	public final void setMinimumDuration( int time ) {
		this.minimumDuration = time;
	}

	/**
	 * This method will log the method duration time if the execution time exceeds specified minimum duration time.
	 * Log format - class-name.method-name  duration-time(in milliseconds)
	 * <p/>
	 * Following is the example of logged entries of MethodExecutionTimeLogger. You can see below all the methods executed for a unique request id with their execution time(in milliseconds).
	 * <ul>
	 * <li>2011-04-21 12:37:06,319 [1303382196403] be.restobookings.services.restaurant.RestaurantService.getRestaurantCountForPartner	594</li>
	 * <li>2011-04-21 12:37:09,351 [1303382196403] be.restobookings.data.model.LocalizationDao.getLocalizedObjects	328</li>
	 * <li>2011-04-21 12:37:09,351 [1303382196403] be.restobookings.services.localization.LocalizationService.getText	328</li>
	 * </ul>
	 *
	 * @param point ProceedingJoinPoint reference from your advice method
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("all")
	protected final Object proceedAndLogExecutionTime( ProceedingJoinPoint point ) throws Throwable {
		long startTime = System.currentTimeMillis();
		Object result = point.proceed();
		long duration = System.currentTimeMillis() - startTime;

		if ( duration > minimumDuration ) {
			String method = "";
			try {
				method = point.getSignature().getDeclaringType().getName() + "." + point.getSignature().getName();
				logger.info( "" + method + "\t" + duration );
			}
			catch ( Exception e ) {
				logger.warn( "unable-to-get-method-signature\n" + duration );
			}

		}
		return result;
	}
}
