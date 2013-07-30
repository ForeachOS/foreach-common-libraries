package com.foreach.spring.logging;

import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * <p>The LogbackConfigurer can be used to configure logback based on one or more
 * xml configuration resources. The configurer must be created as a Spring bean as
 * the actual configuration will be done in the @PostConstruct of the bean.</p>
 * <p>Example usage in Spring {@literal @}Configuration:</p>
 * <pre>
 *   {@literal @}Bean
 *   public LogbackConfigurer logbackConfigurer( {@literal @}Value("${log.dir}") String logDir,
 *                                               {@literal @}Value("${log.config}") Resource baseConfig,
 *                                               {@literal @}Value("${log.config.extend}") Resource envConfig ) {
 *     return new LogbackConfigurer( logDir, baseConfig, envConfig );
 *   }
 * </pre>
 */
public class LogbackConfigurer extends LogbackConfigurerAdapter
{
	private final String logDir;
	private final Collection<Resource> configurationResources;

	/**
	 * Create a configurer with the specified log directory and one or more XML resources.
	 * The XML resources should be passed in the order in which they should be applied.
	 * The log directory passed in can be used in the XML resources as ${log.dir} property.
	 *
	 * @param logDir                 Base directory for log files - will be passed as ${log.dir} property.
	 * @param configurationResources One or more XML (Joran style) resources.
	 */
	public LogbackConfigurer( String logDir, Resource... configurationResources ) {
		this.logDir = logDir;
		this.configurationResources = Arrays.asList( configurationResources );
	}

	@Override
	protected void addProperties( Map<String, String> properties ) {
		properties.put( "log.dir", logDir );
	}

	@Override
	protected void addConfigurationResources( Collection<Resource> resources ) {
		resources.addAll( configurationResources );
	}
}
