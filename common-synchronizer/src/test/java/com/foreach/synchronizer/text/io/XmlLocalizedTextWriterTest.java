package com.foreach.synchronizer.text.io;

import org.junit.Test;

import java.io.ByteArrayOutputStream;


public class XmlLocalizedTextWriterTest {

    @Test
    public void testWrite() throws Exception {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XmlLocalizedTextWriter xmlLocalizedTextWriter = new XmlLocalizedTextWriter(outputStream);

    }
}
