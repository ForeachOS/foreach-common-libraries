package com.foreach.synchronizer.text.actions;

import com.foreach.spring.localization.text.LocalizedText;
import com.foreach.spring.localization.text.LocalizedTextService;
import com.foreach.synchronizer.text.io.LocalizedTextFileHandler;
import com.foreach.synchronizer.text.io.LocalizedTextFormat;
import com.foreach.synchronizer.text.io.LocalizedTextReader;
import com.foreach.synchronizer.text.io.LocalizedTextReaderFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class UploadAction implements SynchronizerAction
{

	public static final String OPTION_INPUT_DIR = "input-dir";
	public static final String SHORT_OPTION_INPUT_DIR = "i";
	public static final String OPTION_FORMAT = "format";
	public static final String SHORT_OPTION_FORMAT = "f";
	private static final LocalizedTextFormat DEFAULT_FORMAT = LocalizedTextFormat.XML;

	@Autowired
	private LocalizedTextFileHandler localizedTextFileHandler;

	@Autowired
	private LocalizedTextReaderFactory localizedTextReaderFactory;

	@Autowired
	private LocalizedTextService localizedTextService;

	public Options getCliOptions()
	{
		Options options = new Options();
		options.addOption( SHORT_OPTION_INPUT_DIR, OPTION_INPUT_DIR, true,
		                   "the input directory to get the files from" );
		options.addOption( SHORT_OPTION_FORMAT, OPTION_FORMAT, true,
		                   "the input format (default=" + DEFAULT_FORMAT.name() + ")" );
		return options;
	}

	public String getActionName()
	{
		return "upload";
	}

	public void execute( CommandLine commandLine ) throws IOException
	{
		String inputDir = commandLine.getOptionValue( OPTION_INPUT_DIR );
		LocalizedTextFormat format = getFormat( commandLine );
		upload( inputDir, format );
	}

	private LocalizedTextFormat getFormat( CommandLine commandLine )
	{
		String formatAsString = commandLine.getOptionValue( OPTION_FORMAT );
		if ( formatAsString == null ) {
			return DEFAULT_FORMAT;
		}
		else {
			return LocalizedTextFormat.valueOf( formatAsString.toUpperCase() );
		}
	}

	public void upload( String inputDirectory, LocalizedTextFormat format )  throws IOException
	{
		List<LocalizedText> localizedTexts = new ArrayList<LocalizedText>();
		List<InputStream> inputStreams = localizedTextFileHandler.getInputStreams( inputDirectory );
		for ( InputStream inputStream : inputStreams ) {
			LocalizedTextReader reader = localizedTextReaderFactory.createLocalizedTextReader( format, inputStream );
			Collection<LocalizedText> texts = reader.read();
			localizedTexts.addAll( texts );
		}
		closeInputStreams( inputStreams );
		for ( LocalizedText localizedText : localizedTexts ) {
			localizedTextService.saveLocalizedText( localizedText );
		}
	}

	public void closeInputStreams( List<InputStream> inputStreams ) throws IOException
	{
		for ( InputStream stream : inputStreams ) {
			stream.close();
		}
	}
}
