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
