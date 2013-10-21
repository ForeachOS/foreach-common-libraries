package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.AbstractLocalizedFieldsObject;
import com.foreach.spring.localization.Language;
import com.foreach.spring.localization.LanguageConfigurator;
import com.foreach.spring.localization.text.LocalizedText;
import com.foreach.spring.localization.text.LocalizedTextFields;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class XmlLocalizedTextReader implements LocalizedTextReader {

    private XStream xStream;
    private final InputStream inputStream;

    public XmlLocalizedTextReader( InputStream inputStream ) {
        this.inputStream = inputStream;
        initializeXStream();
    }

    private void initializeXStream() {
        xStream = new XStream();
        xStream.alias( "text", LocalizedText.class );
        xStream.alias( "fields", LocalizedTextFields.class );
        xStream.addDefaultImplementation( LanguageConfigurator.getLanguageClass(), Language.class );
        xStream.omitField( AbstractLocalizedFieldsObject.class, "fieldsAsUnmodifiableMap" );
        xStream.omitField( AbstractLocalizedFieldsObject.class, "fieldsAsModifiableCollection" );
        xStream.addImplicitMap( LocalizedText.class, "fieldsByLanguageCode", LocalizedTextFields.class, "language" );
    }

    public void close() throws IOException {
        IOUtils.closeQuietly( inputStream );
    }

    public Collection<LocalizedText> read(  ) {
        Collection<LocalizedText> localizedTexts  = (Collection<LocalizedText>) xStream.fromXML( inputStream );
        return localizedTexts;
    }
}
