package com.foreach.spring.logging;

import ch.qos.logback.classic.LoggerContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.Collection;

public class LogbackConfigurer extends LogbackConfigurerAdapter
{
	@Value("${log.dir}")
	private String logDir;

	@Value("${log.config}")
	private Resource baseConfiguration;

	@Value("${log.config.extend}")
	private Resource environmentConfiguration;

	@Override
	protected void putProperties( LoggerContext context ) {
		context.putProperty( "log.dir", logDir );
	}

	@Override
	protected void addConfigurationResources( Collection<Resource> resources ) {
		resources.add( baseConfiguration );
		resources.add( environmentConfiguration );
	}
}
