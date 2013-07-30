package com.foreach.spring.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.apache.commons.lang3.StringUtils;
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

		info( "Initializing logging system with logback configuration files" );

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
				info( "Applying logback configuration {}", resource );
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
			warn( "Exception parsing logback configuration {}: {}", file, jex );
		}
		catch ( Exception e ) {
			throw new LogbackConfigurerException( e );
		}
	}

	private void info( String message, Object... parameters ) {
		System.out.println( String.format( StringUtils.replace( message, "{}", "%s" ), parameters ) );
		LOG.info( message, parameters );
	}

	private void warn( String message, Object... parameters ) {
		System.err.println( String.format( StringUtils.replace( message, "{}", "%s" ), parameters ) );
		LOG.warn( message, parameters );
	}
}
