package com.foreach.web.context;

import com.foreach.web.context.ApplicationEnvironment;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * <p>This class holds the web application properties.
 * This class should be defined as a bean which can be referred in several other beans (e.g. ExceptionToMailResolver).
 * To declare a bean for this class in your spring configuration file do the following.
 * </p>
 *
 * <pre>
 *  &lt;bean id="webApplicationContext" class="com.foreach.web.context.WebApplicationContext"&gt;
 *		&lt;property name="environmentLabel" value="development/test/production"/&gt;
 *		&lt;property name="applicationName" value="name of your application"/&gt;
 *		&lt;property name="label" value="some useful label of your application"/&gt;
 *	    &lt;property name="buildNumber" value="application build version number"/&gt;
 *	    &lt;property name="buildDate" value="build date of the application"/&gt;
 *	&lt;/bean&gt;
 * </pre>
 *
 */
public class WebApplicationContext
{
	private ApplicationEnvironment environment;

	private String applicationName;
    private String label;

	private long buildNumber;
	private Date buildDate, startupDate = new Date();

	public final void setEnvironmentLabel( String environmentLabel )
	{
		environment = ApplicationEnvironment.valueOf( StringUtils.upperCase(environmentLabel) );
	}

	public final ApplicationEnvironment getEnvironment()
	{
		return environment;
	}

	public final void setEnvironment( ApplicationEnvironment environment )
	{
		this.environment = environment;
	}

	public final boolean isRunningIn( ApplicationEnvironment environmentToCheck )
	{
		return environment != null && environment.equals( environmentToCheck );
	}

	public final String getLabel()
	{
		return label;
	}

	public final void setLabel( String label )
	{
		this.label = label;
	}

	public final String getApplicationName()
	{
		return applicationName;
	}

	public final long getBuildNumber()
	{
		return buildNumber != 0 ? buildNumber : getBuildDate().getTime();
	}

	public final void setBuildNumber( long buildNumber )
	{
		this.buildNumber = buildNumber;
	}

	public final Date getStartupDate()
	{
		return startupDate;
	}

	public final Date getBuildDate()
	{
		return buildDate != null ? buildDate : startupDate;
	}

	public final void setBuildDate( Date buildDate )
	{
		this.buildDate = buildDate;
	}

	public final void setApplicationName( String applicationName )
	{
		this.applicationName = applicationName;
	}


	@Override
	public final String toString()
	{
		return "WebApplicationContext{" + "environment=" + environment + ", label='" + label + '\''
                + ", applicationName='" + applicationName + '\'' + ", buildNumber=" + buildNumber + ", buildDate="
                + buildDate + ", startupDate=" + startupDate + '}';
	}

}
