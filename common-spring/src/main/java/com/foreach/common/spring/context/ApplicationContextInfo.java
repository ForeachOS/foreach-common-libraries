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

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * <strong>This class is deprecated, favour the more simple {@link com.foreach.common.spring.context.ApplicationInfo}
 * interface.</strong>
 * This class groups application metadata.
 * <p/>
 * Because some of the metadata may only be available at build time,
 * you will usually have spring instantiate this object as a bean from xml.
 * <p/>
 * You can refer to this bean from other beans that handle errors or provide
 * environment dependent logic.
 * <p/>
 * An example spring configuration:
 * <p/>
 * <pre>
 *  &lt;bean id="applicationContextInfo" class="com.foreach.spring.context.ApplicationContextInfo"&gt;
 *      &lt;property name="environmentLabel" value="${app.environment}"/&gt;
 *      &lt;-- see {@link ApplicationEnvironment} --/&gt;
 *      &lt;property name="applicationName" value="${app.name}"/&gt;
 *      &lt;property name="label" value="${app.label}"/&gt;
 *      &lt;property name="buildNumber" value="${build.number}"/&gt;
 *      &lt;property name="buildDate" value="${build.date}"/&gt;
 *  &lt;/bean&gt;
 * </pre>
 *
 * @see com.foreach.common.spring.context.ApplicationInfo
 * @see com.foreach.common.spring.context.MutableApplicationInfo
 */
public class ApplicationContextInfo implements ApplicationInfo
{
	private String environmentId;

	private String applicationName;
	private String label;
	private String hostName;

	private long buildNumber;
	private Date buildDate, startupDate = new Date();

	/**
	 * Application instance is usually a combination of the application, environment
	 * and hostname.
	 * <p/>
	 * Example: myapp-test-localhost
	 *
	 * @return The unique id of the application instance.
	 */
	@Override
	public String getInstanceId() {
		return getApplicationId() + "-" + getEnvironmentId() + "-" + getHostName();
	}

	/**
	 * Example: myapp
	 *
	 * @return The id of the application.
	 */
	@Override
	public String getApplicationId() {
		return getApplicationName();
	}

	@Override
	public String getHostName() {
		return hostName;
	}

	public void setHostName( String hostName ) {
		this.hostName = hostName;
	}

	/**
	 * Specify {@link ApplicationEnvironment} label for the current environment, the specified label should be one of {@link ApplicationEnvironment}
	 *
	 * @param environmentLabel
	 */
	@Deprecated
	public final void setEnvironmentLabel( String environmentLabel ) {
		setEnvironmentId( StringUtils.upperCase( environmentLabel ) );
	}

	/**
	 * Specify a {@link ApplicationEnvironment} enum for the current environment, the specified enum should be one of {@link ApplicationEnvironment}
	 *
	 * @param environment
	 */
	public final void setEnvironment( ApplicationEnvironment environment ) {
		setEnvironmentId( environment.name() );
	}

	public void setEnvironmentId( String environmentId ) {
		this.environmentId = environmentId;
	}

	/**
	 * Example: test
	 *
	 * @return The id of the environment this application is running in.
	 */
	@Override
	public String getEnvironmentId() {
		return environmentId;
	}

	/**
	 * Example: Test environment
	 *
	 * @return The descriptive name of the environment.
	 */
	@Override
	public String getEnvironmentName() {
		return getEnvironmentId();
	}

	/**
	 * Returns the current application environment
	 */
	@Deprecated
	public final ApplicationEnvironment getEnvironment() {
		return ApplicationEnvironment.valueOf( getEnvironmentId() );
	}

	/**
	 * Method to check whether the current application is running in given mode.
	 * Used for environment dependent logic.
	 *
	 * @param environmentToCheck
	 * @return true if environment is not null and equal to environmentToCheck, false otherwise.
	 */
	public final boolean isRunningIn( ApplicationEnvironment environmentToCheck ) {
		return isRunningIn( environmentToCheck.name() );
	}

	/**
	 * @param environmentId Id of the environment to check against.
	 * @return True if the application is running in that environment.
	 */
	@Override
	public boolean isRunningIn( String environmentId ) {
		return StringUtils.equalsIgnoreCase( environmentId, this.environmentId );
	}

	/**
	 * Specify a readable label for the current application
	 *
	 * @param label
	 */
	public final void setLabel( String label ) {
		this.label = label;
	}

	/**
	 * Returns the specified label of the current application
	 */
	public final String getLabel() {
		return label;
	}

	/**
	 * Specify an application name
	 *
	 * @param applicationName
	 */
	public final void setApplicationName( String applicationName ) {
		this.applicationName = applicationName;
	}

	/**
	 * Returns the specified application name
	 */
	public final String getApplicationName() {
		return applicationName;
	}

	/**
	 * Specify the build number of the current application
	 *
	 * @param buildNumber a number corresponding to a unique build to facilitate
	 *                    defect tracking.
	 */
	public final void setBuildNumber( long buildNumber ) {
		this.buildNumber = buildNumber;
	}

	/**
	 * Returns the build number of the current application
	 */
	public final long getBuildNumber() {
		return buildNumber != 0 ? buildNumber : getBuildDate().getTime();
	}

	/**
	 * Could be a build number from the build server, revision number from source control etc.
	 *
	 * @return The id of the build
	 */
	@Override
	public String getBuildId() {
		return "" + getBuildNumber();
	}

	/**
	 * Specify the build date of current application
	 *
	 * @param buildDate the date the application was built.
	 */
	public final void setBuildDate( Date buildDate ) {
		this.buildDate = buildDate;
	}

	/**
	 * Returns the build date of the current application
	 */
	public final Date getBuildDate() {
		return buildDate != null ? buildDate : startupDate;
	}

	/**
	 * Returns the start-up date of the current application.
	 * This value is set automatically at instantiation.
	 */
	public final Date getStartupDate() {
		return startupDate;
	}

	@Override
	public final String toString() {
		return "ApplicationContextInfo{" + "environment=" + getEnvironmentId() + ", label='" + label + '\'' + ", applicationName='" + applicationName + '\'' + ", buildNumber=" + buildNumber + ", buildDate=" + buildDate + ", startupDate=" + startupDate + '}';
	}

}
