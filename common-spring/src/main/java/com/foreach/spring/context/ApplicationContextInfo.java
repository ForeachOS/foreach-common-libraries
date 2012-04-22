package com.foreach.spring.context;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
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
 */
public class ApplicationContextInfo
{
	private ApplicationEnvironment environment;

	private String applicationName;
	private String label;

	private long buildNumber;
	private Date buildDate, startupDate = new Date();

	/**
	 * Specify {@link ApplicationEnvironment} label for the current environment, the specified label should be one of {@link ApplicationEnvironment}
	 *
	 * @param environmentLabel
	 */
	public final void setEnvironmentLabel( String environmentLabel )
	{
		environment = ApplicationEnvironment.valueOf( StringUtils.upperCase( environmentLabel ) );
	}

	/**
	 * Specify a {@link ApplicationEnvironment} enum for the current environment, the specified enum should be one of {@link ApplicationEnvironment}
	 *
	 * @param environment
	 */
	public final void setEnvironment( ApplicationEnvironment environment )
	{
		this.environment = environment;
	}

	/**
	 * Returns the current application environment
	 */
	public final ApplicationEnvironment getEnvironment()
	{
		return environment;
	}

	/**
	 * Method to check whether the current application is running in given mode.
	 * Used for environment dependent logic.
	 *
	 * @param environmentToCheck
	 * @return true if environment is not null and equal to environmentToCheck, false otherwise.
	 */
	public final boolean isRunningIn( ApplicationEnvironment environmentToCheck )
	{
		return environment != null && environment.equals( environmentToCheck );
	}

	/**
	 * Specify a readable label for the current application
	 *
	 * @param label
	 */
	public final void setLabel( String label )
	{
		this.label = label;
	}

	/**
	 * Returns the specified label of the current application
	 */
	public final String getLabel()
	{
		return label;
	}

	/**
	 * Specify an application name
	 *
	 * @param applicationName
	 */
	public final void setApplicationName( String applicationName )
	{
		this.applicationName = applicationName;
	}

	/**
	 * Returns the specified application name
	 */
	public final String getApplicationName()
	{
		return applicationName;
	}

	/**
	 * Specify the build number of the current application

	 * @param buildNumber a number corresponding to a unique build to facilitate
	 * defect tracking.
	 */
	public final void setBuildNumber( long buildNumber )
	{
		this.buildNumber = buildNumber;
	}

	/**
	 * Returns the build number of the current application
	 */
	public final long getBuildNumber()
	{
		return buildNumber != 0 ? buildNumber : getBuildDate().getTime();
	}

	/**
	 * Specify the build date of current application
	 *
	 * @param buildDate the date the application was built.
	 */
	public final void setBuildDate( Date buildDate )
	{
		this.buildDate = buildDate;
	}

	/**
	 * Returns the build date of the current application
	 */
	public final Date getBuildDate()
	{
		return buildDate != null ? buildDate : startupDate;
	}

	/**
	 * Returns the start-up date of the current application.
	 * This value is set automatically at instantiation.
	 */
	public final Date getStartupDate()
	{
		return startupDate;
	}

	@Override
	public final String toString()
	{
		return "ApplicationContextInfo{" + "environment=" + environment + ", label='" + label + '\'' + ", applicationName='" + applicationName + '\'' + ", buildNumber=" + buildNumber + ", buildDate=" + buildDate + ", startupDate=" + startupDate + '}';
	}

}
