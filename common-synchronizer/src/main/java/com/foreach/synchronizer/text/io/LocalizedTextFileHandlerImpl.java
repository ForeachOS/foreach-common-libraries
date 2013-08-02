package com.foreach.synchronizer.text.io;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class LocalizedTextFileHandlerImpl implements LocalizedTextFileHandler {
	public OutputStream getOutputStream(String outputDirectory, String application, String group, LocalizedTextOutputFormat outputFormat ){
		File outputFile = getOutputFile( outputDirectory, application, group, outputFormat );
		try {
			return new FileOutputStream( outputFile );
		} catch ( IOException e ) {
			e.printStackTrace();
			throw new RuntimeException( "Unable to write to file: " + outputFile.getAbsolutePath() + "!" );
		}
	}

	public File getOutputFile( String outputDirectory, String application, String group, LocalizedTextOutputFormat outputFormat ) {
		File outputFile = new File( outputDirectory, getFileName( application, group, outputFormat ) );
		if ( outputFile.exists() && outputFile.isDirectory() ) {
			throw new RuntimeException( "File " + outputFile.getAbsolutePath() + " is a directory!" );
		}
		return outputFile;
	}

	public String getFileName( String application, String group, LocalizedTextOutputFormat outputFormat ) {
		String fileName = application + "." + group;
		fileName = fileName.toLowerCase() + "." + outputFormat.toString().toLowerCase();
		return fileName;
	}
}
