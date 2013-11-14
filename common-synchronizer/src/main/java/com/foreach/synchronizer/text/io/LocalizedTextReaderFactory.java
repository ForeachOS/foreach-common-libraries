package com.foreach.synchronizer.text.io;

import java.io.InputStream;

public interface LocalizedTextReaderFactory {

    LocalizedTextReader createLocalizedTextReader( LocalizedTextFormat format, InputStream inputStream );

}
