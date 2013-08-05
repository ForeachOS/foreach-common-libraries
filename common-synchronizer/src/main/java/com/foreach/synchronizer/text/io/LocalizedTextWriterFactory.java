package com.foreach.synchronizer.text.io;

import java.io.OutputStream;

public interface LocalizedTextWriterFactory {

	LocalizedTextWriter createLocalizedTextWriter( LocalizedTextOutputFormat format, OutputStream outputStream );

}
