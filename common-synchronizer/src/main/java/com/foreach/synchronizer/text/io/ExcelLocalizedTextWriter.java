package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.text.LocalizedText;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class ExcelLocalizedTextWriter implements LocalizedTextWriter {

    private OutputStream outputStream;

    public ExcelLocalizedTextWriter( OutputStream outputStream ) {
        this.outputStream = outputStream;
    }

    public void write( Collection<LocalizedText> localizedTexts ) {
        //TODO
        throw new RuntimeException( "Not yet implemented" );
    }

    public void close() throws IOException {
        IOUtils.closeQuietly( outputStream );
    }
}
