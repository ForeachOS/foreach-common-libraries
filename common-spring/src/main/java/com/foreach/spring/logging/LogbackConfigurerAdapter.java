package com.foreach.spring.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.LinkedList;

public abstract class LogbackConfigurerAdapter
{
	private final Logger LOG;

	protected LogbackConfigurerAdapter() {
		LOG = LoggerFactory.getLogger( getClass() );
	}

	@PostConstruct
	public void configure() {
		Collection<Resource> configurationResources = new LinkedList<Resource>();

		info( "Initializing logging system with logback configuration files" );

		addConfigurationResources( configurationResources );

		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.reset();

		putProperties( loggerContext );
		parseResources( loggerContext, configurationResources );
	}

	protected void addConfigurationResources( Collection<Resource> resources ) {

	}

	protected void putProperties( LoggerContext context ) {

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
		System.out.println( String.format( message, parameters ) );
		LOG.info( message, parameters );
	}

	private void warn( String message, Object... parameters ) {
		System.err.println( String.format( message, parameters ) );
		LOG.warn( message, parameters );
	}
}
