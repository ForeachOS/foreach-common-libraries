package com.foreach.synchronizer.text.actions;

import com.foreach.spring.localization.text.LocalizedText;
import com.foreach.spring.localization.text.LocalizedTextService;
import com.foreach.synchronizer.text.io.LocalizedTextFileHandler;
import com.foreach.synchronizer.text.io.LocalizedTextOutputFormat;
import com.foreach.synchronizer.text.io.LocalizedTextWriter;
import com.foreach.synchronizer.text.io.LocalizedTextWriterFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class DownloadAction implements SynchronizerAction {

	public static final String OPTION_OUTPUT_DIR = "output-dir";

	@Autowired
	private LocalizedTextWriterFactory localizedTextWriterFactory;

	@Autowired
	private LocalizedTextService localizedTextService;

	@Autowired
	private LocalizedTextFileHandler localizedTextFileHandler;

	public Options getCliOptions() {
		Options options = new Options();
		options.addOption( "o", OPTION_OUTPUT_DIR, true, "the output directory to save the files to" );
		return options;
	}

	public String getActionName() {
		return "download";
	}

	public void execute( CommandLine commandLine ) {
		writeToFiles( commandLine.getOptionValue( OPTION_OUTPUT_DIR ) );
	}


	public void writeToFiles( String outputDirectory ) {
		for ( String application : localizedTextService.getApplications() ) {
			for ( String group : localizedTextService.getGroups( application ) ) {
				OutputStream outputStream = localizedTextFileHandler.getOutputStream( outputDirectory, application, group, LocalizedTextOutputFormat.XML );
				List<LocalizedText> localizedTextItems = localizedTextService.getLocalizedTextItems( application, group );
				LocalizedTextWriter writer = localizedTextWriterFactory.createLocalizedTextWriter( LocalizedTextOutputFormat.XML, outputStream );
				writer.write( localizedTextItems );
				try {
					outputStream.close();
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
		}
	}
}
