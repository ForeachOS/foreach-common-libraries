package com.foreach.web.utils;

import com.foreach.logging.Log4jUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utility class containing web related methods for Log4j
 *
 * @version 1.0
 */
public final class Log4jWebUtils
{

	private Log4jWebUtils()
	{
		// utility classes should not have public/default constructor, so provide default private constructor
	}

	/**
	 * This method generates the HTML content for all the registered loggers in current application.
	 * The returned html content can be directly sent to the browser by defining spring annotation @ResponseBody on the controller method
	 * Eg. HttpServletResponse.getWriter().write( "html content" );
	 *
	 * @param applicationName
	 * @param formAction
	 * @return
	 */
	public String getLoggersHtml( String applicationName, String formAction )
	{
		List<Logger> loggers = Log4jUtils.getClassLoggers();
		List<Level> levels =
				Arrays.<Level>asList( Level.OFF, Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG );

		StringBuffer output = new StringBuffer();
		output.append( "<html><head><title>Manage Loggers - " ).append( applicationName ).append(
				"</title></head><body>" );
		output.append( "<h1>Manage Loggers - " ).append( applicationName ).append( "</h1>" );
		output.append( "<form action='" ).append( formAction ).append( "' method='post'>" );
		output.append( "<table>" );
		output.append( "<tr><td colspan='7'>&nbsp;</td></tr>" ).append( "<tr><td align='center' colspan='7'>" ).append(
				"<input type='reset' value='Reset'><input type='submit' value='Save'></td></tr>" );

		for ( Logger logger : loggers ) {
			output.append( "<tr>" ).append( "<td>" ).append( logger.getName() ).append( "</td>" );
			for ( Level level : levels ) {
				output.append( "<td>" );
				output.append( "<input type='radio' name='" ).append( logger.getName() );
				output.append( "' value='" ).append( level ).append( "' " ).append(
						logger.getEffectiveLevel() == level ? "checked" : "" ).append( " >" ).append( level );
				output.append( "</td>" );
			}
			output.append( "</tr>" );
		}
		output.append( "<tr><td colspan='7'>&nbsp;</td></tr>" ).append(
				"<tr><td align='center' colspan='7'><input type='reset' value='Reset'><input type='submit' value='Save'></td></tr>" );
		output.append( "</table>" );
		output.append( "</form>" );
		output.append( "</body></html>" );

		return output.toString();
	}

	/**
	 * This method will update the levels of registered Loggers to the new levels retrieved from the given HttpServletRequest object
	 *
	 * @param request
	 */
	public void setLoggerLevels( HttpServletRequest request )
	{
		Map<String, String[]> params = request.getParameterMap();
		for ( Map.Entry<String, String[]> entry : params.entrySet() ) {
			String name = entry.getKey();
			if ( LogManager.exists( name ) != null ) {
				String[] levels = entry.getValue();
				Level level = Level.toLevel( levels[0] );
				Logger.getLogger( name ).setLevel( level );
			}
		}
	}

}
