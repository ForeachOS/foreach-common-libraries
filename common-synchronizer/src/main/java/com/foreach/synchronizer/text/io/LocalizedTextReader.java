package com.foreach.synchronizer.text.io;

import com.foreach.common.spring.localization.text.LocalizedText;

import java.io.Closeable;
import java.util.Collection;

public interface LocalizedTextReader extends Closeable {
    Collection<LocalizedText> read(  );
}
