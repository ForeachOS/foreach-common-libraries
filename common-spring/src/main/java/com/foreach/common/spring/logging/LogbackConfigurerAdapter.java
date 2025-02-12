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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Adapter class for creating beans that make it easier to configure logback using
 * XML resources (classpath, file resources).  Adapted methods should provide the resource references
 * and properties to use, as well as do custom LoggerContext configuration.
 */
public abstract class LogbackConfigurerAdapter
{
	private final Logger LOG;

	protected LogbackConfigurerAdapter() {
		LOG = LoggerFactory.getLogger( getClass() );
	}

	/**
	 * Applies the logback configuration.  In case of a Spring bean, this method will be called
	 * as a PostConstruct.
	 */
	@PostConstruct
	public void configure() {
		Collection<Resource> configurationResources = new LinkedList<Resource>();

		LOG.info( "Initializing logging system with logback configuration files" );

		addConfigurationResources( configurationResources );

		if ( !configurationResources.isEmpty() ) {
			// If no resources specified, don't do anything
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			loggerContext.reset();

			configureLoggerContext( loggerContext );

			Map<String, String> properties = new HashMap<String, String>();
			addProperties( properties );

			for ( Map.Entry<String, String> entry : properties.entrySet() ) {
				loggerContext.putProperty( entry.getKey(), entry.getValue() );
			}

			parseResources( loggerContext, configurationResources );
		}
	}

	/**
	 * Perform special configuration on the LoggerContext in this method.
	 * <strong>Empty implementation.</strong>
	 *
	 * @param context Logback LoggerContext instance.
	 */
	protected void configureLoggerContext( LoggerContext context ) {

	}

	/**
	 * Add XML configuration resources in this method.
	 * <strong>Empty implementation.</strong>
	 *
	 * @param resources Collection where XML resources should be added.
	 */
	protected void addConfigurationResources( Collection<Resource> resources ) {

	}

	/**
	 * Add configuration properties to be passed to the XML resources in this method.
	 * <strong>Empty implementation.</strong>
	 *
	 * @param properties Map where properties should be added.
	 */
	protected void addProperties( Map<String, String> properties ) {

	}

	private void parseResources( LoggerContext context, Collection<Resource> resources ) {
		for ( Resource resource : resources ) {
			if ( resource != null && resource.exists() ) {
				LOG.info( "Applying logback configuration {}", resource );
				parseLogbackFile( context, resource );
			}
		}
	}

	private void parseLogbackFile( LoggerContext loggerContext, Resource file ) {
		JoranConfigurator joranConfigurator = new JoranConfigurator();
		try {
			joranConfigurator.setContext( loggerContext );
			joranConfigurator.doConfigure( file.getInputStream() );
		}
		catch ( JoranException jex ) {
			LOG.warn( "Exception parsing logback configuration {}: {}", file, jex );
		}
		catch ( Exception e ) {
			throw new LogbackConfigurerException( e );
		}
	}
}
