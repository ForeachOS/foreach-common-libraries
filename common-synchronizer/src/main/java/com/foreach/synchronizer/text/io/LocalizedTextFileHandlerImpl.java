package com.foreach.synchronizer.text.io;

import com.foreach.synchronizer.text.TextSynchronizerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class LocalizedTextFileHandlerImpl implements LocalizedTextFileHandler {

    private static final Logger LOG = LoggerFactory.getLogger( LocalizedTextFileHandlerImpl.class );

    public List<InputStream> getInputStreams(String inputDirectory)
    {
        List<InputStream> inputStreams = new ArrayList<InputStream>(  );
        File[] inputFiles = getInputFiles( inputDirectory );
        for(File inputFile: inputFiles)
        {
            try {
                inputStreams.add( new FileInputStream( inputFile ));
            } catch ( FileNotFoundException e ) {
                LOG.error( e.getMessage(), e );
                throw new TextSynchronizerException( "Unable to find to file: " + inputFile.getAbsolutePath() + "!" );
            }
        }
        return inputStreams;
    }

    public File[] getInputFiles(String inputDirectory)
    {
        File directory = new File( inputDirectory );
        if(!directory.isDirectory() )
        {
            return null;
        }
        return directory.listFiles();
    }

    public OutputStream getOutputStream( String outputDirectory, String application, String group, LocalizedTextFormat outputFormat ) {
        File outputFile = getOutputFile( outputDirectory, application, group, outputFormat );
        try {
            return new FileOutputStream( outputFile );
        } catch ( IOException e ) {
            LOG.error( e.getMessage(), e );
            throw new TextSynchronizerException( "Unable to write to file: " + outputFile.getAbsolutePath() + "!" );
        }
    }

    public File getOutputFile( String outputDirectory, String application, String group, LocalizedTextFormat outputFormat ) {
        File outputFile = new File( getDirectory( outputDirectory ), getFileName( application, group, outputFormat ) );
        if( outputFile.exists() && outputFile.isDirectory() ) {
            throw new TextSynchronizerException( "File " + outputFile.getAbsolutePath() + " is a directory!" );
        }
        return outputFile;
    }

    private File getDirectory( String directoryAsString ) {
        File directory = new File( directoryAsString );
        if( directory.exists() ) {
            if( !directory.isDirectory() ) {
                throw new TextSynchronizerException( "Directory " + directory.getAbsolutePath() + " is not a directory!" );
            }
        } else {
            boolean success = directory.mkdirs();
            if( !success ) {
                throw new TextSynchronizerException( "Failed to create directory " + directory.getAbsolutePath() + "!" );
            }
        }
        return directory;
    }

    public String getFileName( String application, String group, LocalizedTextFormat outputFormat ) {
        String fileName = application + "." + group;
        fileName = fileName.toLowerCase() + "." + outputFormat.toString().toLowerCase();
        return fileName;
    }
}
