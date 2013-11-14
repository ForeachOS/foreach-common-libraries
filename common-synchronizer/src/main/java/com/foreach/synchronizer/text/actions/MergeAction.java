package com.foreach.synchronizer.text.actions;

import com.foreach.spring.localization.text.LocalizedText;
import com.foreach.synchronizer.text.LocalizedTextWrapper;
import com.foreach.synchronizer.text.io.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Component
public class MergeAction implements SynchronizerAction {

    public static final String OPTION_OUTPUT_DIR = "output-dir";
    public static final String SHORT_OPTION_OUTPUT_DIR = "o";
    public static final String OPTION_INPUT_DIR = "input-dir";
    public static final String SHORT_OPTION_INPUT_DIR = "i";
    public static final String OPTION_FORMAT = "format";
    public static final String SHORT_OPTION_FORMAT = "f";
    private static final LocalizedTextFormat DEFAULT_FORMAT = LocalizedTextFormat.XML;

    @Autowired
    private LocalizedTextFileHandler localizedTextFileHandler;

    @Autowired
    private LocalizedTextWriterFactory localizedTextWriterFactory;

    @Autowired
    private LocalizedTextReaderFactory localizedTextReaderFactory;

    public Options getCliOptions() {
        Options options = new Options();
        options.addOption( SHORT_OPTION_OUTPUT_DIR, OPTION_OUTPUT_DIR, true,
                "the output directory to save the files to" );
        options.addOption( SHORT_OPTION_INPUT_DIR, OPTION_INPUT_DIR, true,
                "the input directory to get the files from" );
        options.addOption( SHORT_OPTION_FORMAT, OPTION_FORMAT, true,
                "the output format (default=" + DEFAULT_FORMAT.name() + ")" );
        return options;
    }

    public String getActionName() {
        return "merge";
    }

    public void execute( CommandLine commandLine ) {
        String outputDir = commandLine.getOptionValue( OPTION_OUTPUT_DIR );
        String[] inputDirs = commandLine.getOptionValues( OPTION_INPUT_DIR );
        LocalizedTextFormat format = getFormat( commandLine );

        merge( outputDir, inputDirs, format );
    }

    private LocalizedTextFormat getFormat( CommandLine commandLine ) {
        String formatAsString = commandLine.getOptionValue( OPTION_FORMAT );
        if( formatAsString == null ) {
            return DEFAULT_FORMAT;
        } else {
            return LocalizedTextFormat.valueOf( formatAsString.toUpperCase() );
        }
    }

    public void merge( String outputDir, String[] inputDirs, LocalizedTextFormat format ) {

        HashMap<LocalizedTextWrapper, LocalizedTextWrapper> mergedTexts = getMergedTextsCollection( inputDirs, format );
        HashMap<String, List<LocalizedText>> orderedTexts = sortByApplication( mergedTexts, inputDirs.length );

        for( List<LocalizedText> textList : orderedTexts.values() ) {
            String application = textList.get( 0 ).getApplication();
            String group = textList.get( 0 ).getGroup();
            LocalizedTextWriter writer = null;
            try {
                OutputStream outputStream =
                        localizedTextFileHandler.getOutputStream( outputDir, application, group, format );
                writer = localizedTextWriterFactory.createLocalizedTextWriter( format, outputStream );
                writer.write( textList );
            } finally {
                IOUtils.closeQuietly( writer );
            }
        }
    }

    private HashMap<String, List<LocalizedText>> sortByApplication( HashMap<LocalizedTextWrapper, LocalizedTextWrapper> mergedTexts, int amountOfEnvironments ) {
        HashMap<String, List<LocalizedText>> orderedTexts = new HashMap<String, List<LocalizedText>>();

        for( LocalizedTextWrapper textWrapper : mergedTexts.values() ) {
            if( textWrapper.shouldUpdate( amountOfEnvironments ) ) {
                LocalizedText text = textWrapper.getLocalizedText();
                String key = getUniqueName( text );
                if( !orderedTexts.containsKey( key ) ) {
                    orderedTexts.put( key, new ArrayList<LocalizedText>() );
                }
                orderedTexts.get( key ).add( text );
            }
        }
        return orderedTexts;
    }

    private String getUniqueName( LocalizedText text ) {
        return text.getApplication() + "." + text.getGroup();
    }

    private HashMap<LocalizedTextWrapper, LocalizedTextWrapper> getMergedTextsCollection(
            String[] inputDirs, LocalizedTextFormat format ) {
        //we have to use a HashMap instead of a hashSet, because a HashSet doesn't have a get method
        HashMap<LocalizedTextWrapper, LocalizedTextWrapper> textsCollection =
                new HashMap<LocalizedTextWrapper, LocalizedTextWrapper>();
        for( String inputDir : inputDirs ) {
            List<InputStream> inputStreams = localizedTextFileHandler.getInputStreams( inputDir );
            for( InputStream inputStream : inputStreams ) {
                LocalizedTextReader reader = localizedTextReaderFactory.createLocalizedTextReader( format, inputStream );
                Collection<LocalizedText> texts = reader.read();
                for( LocalizedText text : texts ) {
                    LocalizedTextWrapper textWrapper = new LocalizedTextWrapper( text );
                    if( textsCollection.containsKey( textWrapper ) ) {
                        textsCollection.get( textWrapper ).merge( text );
                    } else {
                        textsCollection.put( textWrapper, textWrapper );
                    }
                }
            }
        }
        return textsCollection;
    }
}
