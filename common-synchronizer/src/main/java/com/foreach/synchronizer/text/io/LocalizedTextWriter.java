package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.text.LocalizedText;

import java.io.Closeable;
import java.util.Collection;

public interface LocalizedTextWriter extends Closeable {
	void write( Collection<LocalizedText> localizedTexts );
}
