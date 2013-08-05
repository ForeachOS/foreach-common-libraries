package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.text.LocalizedText;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class XmlLocalizedTextWriter implements LocalizedTextWriter {

	private XStream xStream;
	private final OutputStream outputStream;

	public XmlLocalizedTextWriter( OutputStream outputStream ) {
		this.outputStream = outputStream;
		initializeXStream();
	}

	private void initializeXStream() {
		xStream = new XStream();
		//TODO configure aliases and stuff
	}

	public void write( Collection<LocalizedText> localizedTexts ) {
		xStream.toXML( localizedTexts, outputStream );
	}

	public void close() throws IOException {
		IOUtils.closeQuietly( outputStream );
	}
}
