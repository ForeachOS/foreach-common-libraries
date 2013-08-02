package com.foreach.synchronizer.text.io;

import java.io.File;
import java.io.OutputStream;

public interface LocalizedTextFileHandler {
	OutputStream getOutputStream( String outputDirectory, String application, String group, LocalizedTextOutputFormat outputFormat );

	File getOutputFile( String outputDirectory, String application, String group, LocalizedTextOutputFormat outputFormat );

	String getFileName( String application, String group, LocalizedTextOutputFormat outputFormat );
}
