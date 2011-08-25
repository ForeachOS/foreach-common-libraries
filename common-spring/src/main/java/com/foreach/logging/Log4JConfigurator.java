package com.foreach.logging;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URL;

/**
 * <p>The Log4JConfigurator bean can be used to configure log4j settings in Spring.
 * It takes the path to the log4j properties file, and the directory where logfiles should be written.
 * The directory is then configured as system property <strong>log.dir</strong> so it can also be used
 * in the log4j properties file.</p>
 * <p>Log4J is configured when the configure method of the bean is called.</p>
 * <p><em>Example use in spring configuration file:</em></p>
 * <pre>
 * &lt;bean class="com.foreach.spring.logging.Log4JConfigurator" init-method="configure" lazy-init="false"&gt;
 *   &lt;property name="propertiesFile" value="classpath:/config/log.properties"/&gt;
 *   &lt;property name="logDirectory" value="/project-logging-dir"/&gt;
 * &lt;/bean&gt;
 * </pre>
 * <p><em>Example excerpt of log.properties (depends on apache-log4j-extras):</em></p>
 * <pre>
 *  log4j.appender.logfile=org.apache.log4j.rolling.RollingFileAppender
 *  log4j.appender.logfile.rollingPolicy=org.apache.log4j.rolling.TimeBasedRollingPolicy
 *  log4j.appender.logfile.rollingPolicy.FileNamePattern=${log.dir}/logfile-%d{yyyy-MM-dd}.log
 * </pre>
 *
 * @author <a href="mailto:pkpk@foreach.be">Pavan K. Parankusam</a>, <a href="mailto:arne@foreach.be">Arne Vandamme</a>
 * @version 1.0
 */
public class Log4JConfigurator
{
    private String logDirectory = System.getProperty("java.io.tmpdir");
    private Resource propertiesFile = null;

    /**
     * <p>Configures Log4J using a <code>PropertyConfigurator</code> and the properties file set on the bean.</p>
     *
     * @throws java.io.IOException if something goes wrong with reading the properties file.
     */
    public final void configure() throws IOException
    {
        if (propertiesFile != null)
        {
            URL url = propertiesFile.getURL();
            PropertyConfigurator.configure(url);
        }
    }

    /**
     * <p>Getter for the field <code>propertiesFile</code>.</p>
     *
     * @return a {@link org.springframework.core.io.Resource} object.
     */
    public final Resource getPropertiesFile()
    {
        return propertiesFile;
    }

    /**
     * <p>Setter for the Log4J configuration file (properties format).</p>
     *
     * @param location a {@link org.springframework.core.io.Resource} object.
     */
    public final void setPropertiesFile(Resource location)
    {
        this.propertiesFile = location;
    }

    /**
     * <p>Getter for the field <code>logDirectory</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public final String getLogDirectory()
    {
        return logDirectory;
    }

    /**
     * <p>Setter for the field <code>logDirectory</code>.  This value will be exposed to the Log4J properties file
     * using the system property <code>log.dir</code>.</p>
     *
     * @param logDirectory a {@link java.lang.String} object.
     */
    public final void setLogDirectory(String logDirectory)
    {
        if (StringUtils.isNotBlank(logDirectory))
        {
            this.logDirectory = logDirectory;

            // Allow log4j properties file to use this property
            System.setProperty("log.dir", this.logDirectory);
        }
    }
}

