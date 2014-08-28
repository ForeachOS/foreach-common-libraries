package com.foreach.synchronizer.text.io;

import com.foreach.common.spring.localization.text.LocalizedText;
import com.foreach.synchronizer.text.PersistentLanguageText;
import com.foreach.synchronizer.text.PersistentLocalizedText;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XmlLocalizedTextWriter implements LocalizedTextWriter {

    private XStream xStream;
    private final OutputStream outputStream;

    public XmlLocalizedTextWriter( OutputStream outputStream ) {
        this.outputStream = outputStream;
        initializeXStream();
    }

    private void initializeXStream() {
        xStream = new XStream();
        xStream.alias( "text", PersistentLocalizedText.class );
        xStream.alias( "fields", PersistentLanguageText.class );
        xStream.addImplicitMap( PersistentLocalizedText.class, "languageTexts", PersistentLanguageText.class, "language" );
    }

    public void write( List<LocalizedText> localizedTexts ) {
        if( localizedTexts == null ) {
            return;
        }
        if(localizedTexts.isEmpty())
        {
            return;
        }
        Collections.sort( localizedTexts );
        List<PersistentLocalizedText> persistentLocalizedTexts = new ArrayList<PersistentLocalizedText>();
        for( LocalizedText localizedText : localizedTexts ) {
            persistentLocalizedTexts.add( new PersistentLocalizedText( localizedText ) );
        }
        try {
            Writer writer = new OutputStreamWriter( outputStream, "UTF-8" );
            xStream.toXML( persistentLocalizedTexts, writer );
        } catch ( UnsupportedEncodingException e ) {
            throw new RuntimeException( "UTF-8 encoding not supported!" );
        }
    }

    public void close() throws IOException {
        IOUtils.closeQuietly( outputStream );
    }
}
