package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.AbstractLocalizedFieldsObject;
import com.foreach.spring.localization.Language;
import com.foreach.spring.localization.LanguageConfigurator;
import com.foreach.spring.localization.text.LocalizedText;
import com.foreach.spring.localization.text.LocalizedTextFields;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class XmlLocalizedTextWriter implements LocalizedTextWriter {

    private XStream xStream;
    private final OutputStream outputStream;

    public XmlLocalizedTextWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
        initializeXStream();
    }

    private void initializeXStream() {
        xStream = new XStream();
        xStream.alias("text", LocalizedText.class);
        xStream.alias("fields", LocalizedTextFields.class);
        xStream.addDefaultImplementation(LanguageConfigurator.getLanguageClass(), Language.class);
        xStream.omitField(AbstractLocalizedFieldsObject.class, "fieldsAsUnmodifiableMap");
        xStream.omitField(AbstractLocalizedFieldsObject.class, "fieldsAsModifiableCollection");
        xStream.addImplicitMap(LocalizedText.class, "fieldsByLanguageCode", LocalizedTextFields.class, "language");
    }

    public void write(Collection<LocalizedText> localizedTexts) {
        xStream.toXML(localizedTexts, outputStream);
    }

    public void close() throws IOException {
        IOUtils.closeQuietly(outputStream);
    }
}
