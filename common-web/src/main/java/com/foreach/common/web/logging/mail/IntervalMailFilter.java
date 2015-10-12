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
package com.foreach.common.web.logging.mail;

/**
 * Use this filter when you want to send mail at specified interval.
 * Only one mail will be sent in the specified interval
 * @author pavan
 */
public class IntervalMailFilter implements MailFilter
{
	private long intervalInSeconds;

	private long lastSentAt;

	/**
	 * Create mail filter which will send one mail at specified interval
	 * @param intervalInSeconds specify interval time in seconds
	 */
	public IntervalMailFilter(long intervalInSeconds)
	{
		this.intervalInSeconds = intervalInSeconds;
	}

	@Override
	public boolean evaluate() {
		if (lastSentAt == 0 || intervalInSeconds == 0){
			lastSentAt = System.currentTimeMillis();
			return true;
		}

		long diff = (System.currentTimeMillis() - lastSentAt);
		long intervalPeriodInMillis = (intervalInSeconds * 1000);
		if (diff > intervalPeriodInMillis){
			lastSentAt = System.currentTimeMillis();
			return true;
		}

		return false;
	}

	public long getIntervalInSeconds() {
		return intervalInSeconds;
	}

}
