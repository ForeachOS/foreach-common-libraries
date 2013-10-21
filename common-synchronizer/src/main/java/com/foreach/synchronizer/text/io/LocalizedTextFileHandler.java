package com.foreach.synchronizer.text.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface LocalizedTextFileHandler {
    OutputStream getOutputStream( String outputDirectory, String application, String group, LocalizedTextFormat outputFormat );

    List<InputStream> getInputStreams(String inputDirectory);
}
