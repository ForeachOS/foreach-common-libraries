package com.foreach.synchronizer.text.io;

import com.foreach.synchronizer.text.TextSynchronizerException;
import org.springframework.stereotype.Component;

import java.io.OutputStream;

@Component
public class LocalizedTextWriterFactoryImpl implements LocalizedTextWriterFactory {
    public LocalizedTextWriter createLocalizedTextWriter( LocalizedTextOutputFormat format, OutputStream outputStream ) {
        switch (format) {
            case XML:
                return new XmlLocalizedTextWriter( outputStream );
            case EXCEL:
                return new ExcelLocalizedTextWriter( outputStream );
            default:
                throw new TextSynchronizerException( "Unexpected format " + format + " !" );
        }
    }
}
