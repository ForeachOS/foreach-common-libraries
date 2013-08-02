package com.foreach.synchronizer.text.io;

import java.io.OutputStream;

public class LocalizedTextWriterFactoryImpl implements LocalizedTextWriterFactory {
    public LocalizedTextWriter createLocalizedTextWriter(LocalizedTextOutputFormat format, OutputStream outputStream) {
        switch (format) {
            case XML:
                return new XmlLocalizedTextWriter(outputStream);
            case EXCEL:
                return new ExcelLocalizedTextWriter(outputStream);
            default:
                throw new RuntimeException("Unexpected format " + format + " !");
        }
    }
}
