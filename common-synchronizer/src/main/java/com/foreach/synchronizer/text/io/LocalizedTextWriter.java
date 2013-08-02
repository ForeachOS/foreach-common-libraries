package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.text.LocalizedText;

import java.util.Collection;

public interface LocalizedTextWriter {
	void write(String application, String group, Collection<LocalizedText> localizedTexts );
}
