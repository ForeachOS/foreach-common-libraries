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
