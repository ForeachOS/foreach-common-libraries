package com.foreach.synchronizer.text.io;

import com.foreach.common.spring.localization.text.LocalizedText;
import com.foreach.synchronizer.text.PersistentLanguageText;
import com.foreach.synchronizer.text.PersistentLocalizedText;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XmlLocalizedTextReader implements LocalizedTextReader {

    private XStream xStream;
    private final InputStream inputStream;

    public XmlLocalizedTextReader( InputStream inputStream ) {
        this.inputStream = inputStream;
        initializeXStream();
    }

    private void initializeXStream() {
        xStream = new XStream();
        xStream.alias( "text", PersistentLocalizedText.class );
        xStream.alias( "fields", PersistentLanguageText.class );
        xStream.addImplicitMap( PersistentLocalizedText.class, "languageTexts", PersistentLanguageText.class, "language" );
    }

    public void close() throws IOException {
        IOUtils.closeQuietly( inputStream );
    }

    public Collection<LocalizedText> read(  ) {
        try {
            Reader reader = new InputStreamReader( inputStream, "UTF-8" );
            Collection<PersistentLocalizedText> persistentLocalizedTexts  = (Collection<PersistentLocalizedText>) xStream.fromXML( reader );
            List<LocalizedText> localizedTexts = new ArrayList<LocalizedText>(  );
            for(PersistentLocalizedText persistentLocalizedText: persistentLocalizedTexts)
            {
                localizedTexts.add( persistentLocalizedText.convert() );
            }
            return localizedTexts;
        } catch ( UnsupportedEncodingException e ) {
            throw new RuntimeException( "UTF-8 encoding not supported!" );
        }
    }
}
