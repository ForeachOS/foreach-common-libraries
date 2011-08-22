package com.foreach.context;

import org.apache.commons.lang.StringUtils;

import java.util.Date;

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
