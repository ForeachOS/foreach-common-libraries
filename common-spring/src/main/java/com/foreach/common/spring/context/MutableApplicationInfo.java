package com.foreach.common.spring.context;

import java.util.Date;

public interface MutableApplicationInfo extends ApplicationInfo
{
	void setInstanceId( String instanceId );

	void setApplicationId( String applicationId );

	void setApplicationName( String applicationName );

	void setEnvironmentId( String environmentId );

	void setEnvironmentName( String environmentName );

	void setHostName( String hostname );

	void setBuildId( String buildId );

	void setBuildDate( Date buildDate );

	void setStartupDate( Date startupDate );
}
