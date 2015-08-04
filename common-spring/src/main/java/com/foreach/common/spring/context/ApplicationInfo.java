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
package com.foreach.common.spring.context;

import java.util.Date;

/**
 * Base interface to hold information about the running application.
 */
public interface ApplicationInfo
{
	/**
	 * Application instance is usually a combination of the application, environment
	 * and hostname.
	 *
	 * Example: myapp-test-localhost
	 *
	 * @return The unique id of the application instance.
	 */
	String getInstanceId();

	/**
	 * Example: myapp
	 *
	 * @return The id of the application.
	 */
	String getApplicationId();

	/**
	 * Example: My Application
	 *
	 * @return The descriptive name of the application.
	 */
	String getApplicationName();

	/**
	 * Example: test
	 *
	 * @return The id of the environment this application is running in.
	 */
	String getEnvironmentId();

	/**
	 * Example: Test environment
	 *
	 * @return The descriptive name of the environment.
	 */
	String getEnvironmentName();

	/**
	 * Example: localhost
	 *
	 * @return The name of infrastructure hosting this application.
	 */
	String getHostName();

	/**
	 * Could be a build number from the build server, revision number from source control etc.
	 *
	 * @return The id of the build
	 */
	String getBuildId();

	/**
	 * @return Timestamp of the build.
	 */
	Date getBuildDate();

	/**
	 * @return Timestamp when the application was considered to be started up.
	 */
	Date getStartupDate();

	/**
	 * @param environmentId Id of the environment to check against.
	 * @return True if the application is running in that environment.
	 */
	boolean isRunningIn( String environmentId );
}
