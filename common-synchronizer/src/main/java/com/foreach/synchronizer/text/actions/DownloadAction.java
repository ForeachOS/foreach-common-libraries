package com.foreach.synchronizer.text.actions;

import com.foreach.spring.localization.text.LocalizedText;
import com.foreach.spring.localization.text.LocalizedTextService;
import com.foreach.synchronizer.text.io.LocalizedTextFileHandler;
import com.foreach.synchronizer.text.io.LocalizedTextFormat;
import com.foreach.synchronizer.text.io.LocalizedTextWriter;
import com.foreach.synchronizer.text.io.LocalizedTextWriterFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.List;

@Component
public class DownloadAction implements SynchronizerAction {

    public static final String OPTION_OUTPUT_DIR = "output-dir";
    public static final String SHORT_OPTION_OUTPUT_DIR = "o";
    public static final String OPTION_FORMAT = "format";
    public static final String SHORT_OPTION_FORMAT = "f";
    private static final LocalizedTextFormat DEFAULT_FORMAT = LocalizedTextFormat.XML;

    @Autowired
    private LocalizedTextWriterFactory localizedTextWriterFactory;

    @Autowired
    private LocalizedTextService localizedTextService;

    @Autowired
    private LocalizedTextFileHandler localizedTextFileHandler;

    public Options getCliOptions() {
        Options options = new Options();
        options.addOption( SHORT_OPTION_OUTPUT_DIR, OPTION_OUTPUT_DIR, true, "the output directory to save the files to" );
        options.addOption( SHORT_OPTION_FORMAT, OPTION_FORMAT, true, "the output format (default=" + DEFAULT_FORMAT.name() + ")" );
        return options;
    }

    public String getActionName() {
        return "download";
    }

    public void execute( CommandLine commandLine ) {
        String outputDir = commandLine.getOptionValue( OPTION_OUTPUT_DIR );
        LocalizedTextFormat outputFormat = getOutputFormat( commandLine );
        writeToFiles( outputDir, outputFormat );
    }

    private LocalizedTextFormat getOutputFormat( CommandLine commandLine ) {
        String formatAsString = commandLine.getOptionValue( OPTION_FORMAT );
        if( formatAsString == null ) {
            return DEFAULT_FORMAT;
        } else {
            return LocalizedTextFormat.valueOf( formatAsString.toUpperCase() );
        }
    }

    public void writeToFiles( String outputDirectory, LocalizedTextFormat outputFormat ) {
        for( String application : localizedTextService.getApplications() ) {
            for( String group : localizedTextService.getGroups( application ) ) {
                LocalizedTextWriter writer = null;
                try {
                    List<LocalizedText> localizedTextItems = localizedTextService.getLocalizedTextItems( application, group );
                    if(localizedTextItems!=null && !localizedTextItems.isEmpty())
                    {
                        //we use applicationForFilename to prevent files in resto to be name 'default.'... as this is a fake application name returned by the DAO
                        String applicationForFilename = localizedTextItems.get(0).getApplication();

                        OutputStream outputStream = localizedTextFileHandler.getOutputStream( outputDirectory, applicationForFilename, group, outputFormat );
                        writer = localizedTextWriterFactory.createLocalizedTextWriter( outputFormat, outputStream );
                        writer.write( localizedTextItems );
                    }
                } finally {
                    IOUtils.closeQuietly( writer );
                }
            }
        }
    }
}
