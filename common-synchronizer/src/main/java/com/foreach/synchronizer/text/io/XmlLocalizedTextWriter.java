package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.text.LocalizedText;
import com.thoughtworks.xstream.XStream;

import java.io.OutputStream;
import java.util.Collection;

public class XmlLocalizedTextWriter implements LocalizedTextWriter {

    private XStream xStream;
    private OutputStream outputStream;

    public XmlLocalizedTextWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.xStream = initializeXStream();
    }

    private XStream initializeXStream() {
        XStream xStream = new XStream();
        //TODO configure aliases and stuff
        return xStream;
    }

    public void write(String application, String group, Collection<LocalizedText> localizedTexts) {
        for (LocalizedText text : localizedTexts) {
            xStream.toXML(text, outputStream);
        }
    }

}
