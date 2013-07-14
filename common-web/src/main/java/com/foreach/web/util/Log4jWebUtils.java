package com.foreach.web.util;

import com.foreach.spring.logging.Log4JUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class contains methods to display and change the log levels of registered Log4j loggers dynamically with an http client.
 * <p/>
 * To configure log levels dynamically, do the following:
 * <ul>
 * <li>In your controller, define a get method that will serve the update form to the client.
 * Use the method {@link #getLoggersHtmlContent} to obtain the html content to be served.</li>
 * <li>Define a post method to process the client response. From this routine you need only call
 * {@link #setLoggerLevels} to update the log levels to the user provided values.</li>
 * </ul>
 *
 * @version 1.0
 */
public class Log4jWebUtils
{
	protected Log4jWebUtils() {
		// protected constructor, so that this class can be extended.
	}

	/**
	 * This method generates the html content for a web page containing a form to change the log level of registered log4j loggers.
	 * The html body can be directly sent to the browser by defining spring annotation @ResponseBody on a controller method.
	 * <p/>
	 * Eg. HttpServletResponse.getWriter().write( "html content" );
	 * <p/>
	 * Use this method to display the registered loggers and their current log level.
	 *
	 * @param applicationName a label to be used in the html body, so the user knows which application's loggers are shown.
	 * @param formAction      the url that maps to the controller method that calls
	 *                        {@link #setLoggerLevels}
	 * @param includePageTags true to include html start/end tags (eg. '<html><body></body></html>'), false to generate only html form content
	 * @return a string with html content.
	 */
	public static String getLoggersHtmlContent( String applicationName, String formAction, boolean includePageTags ) {
		List<Logger> loggers = Log4JUtils.getClassLoggers();
		List<Level> levels =
				Arrays.<Level>asList( Level.OFF, Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG );

		StringBuffer output = new StringBuffer();

		if ( includePageTags ) {
			output.append( "<html><head><title>Manage Loggers - " ).append( applicationName ).append(
					"</title></head><body>" );
		}

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

		if ( includePageTags ) {
			output.append( "</body></html>" );
		}

		return output.toString();
	}

	/**
	 * This method will update the levels of registered Loggers to the new levels
	 * retrieved from the given HttpServletRequest object.
	 * <p/>
	 * You should call this method from within the controller method that maps to
	 * the formAction parameter you provided to {@link #getLoggersHtmlContent}.
	 *
	 * @param request the http request object containing the updated log levels.
	 */
	public static void setLoggerLevels( HttpServletRequest request ) {
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
