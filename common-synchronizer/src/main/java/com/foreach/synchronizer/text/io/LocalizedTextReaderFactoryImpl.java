package com.foreach.synchronizer.text.io;

import com.foreach.synchronizer.text.TextSynchronizerException;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class LocalizedTextReaderFactoryImpl implements LocalizedTextReaderFactory {
    public LocalizedTextReader createLocalizedTextReader( LocalizedTextFormat format, InputStream inputStream ) {
        switch (format) {
            case XML:
                return new XmlLocalizedTextReader( inputStream );
//            case EXCEL:
//                return new ExcelLocalizedTextReader( inputStream );
            default:
                throw new TextSynchronizerException( "Unexpected format " + format + " !" );
        }
    }
}
