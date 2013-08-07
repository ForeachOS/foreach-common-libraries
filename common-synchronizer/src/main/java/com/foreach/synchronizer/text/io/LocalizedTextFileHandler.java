package com.foreach.synchronizer.text.io;

import java.io.OutputStream;

public interface LocalizedTextFileHandler {
	OutputStream getOutputStream( String outputDirectory, String application, String group, LocalizedTextOutputFormat outputFormat );
}
