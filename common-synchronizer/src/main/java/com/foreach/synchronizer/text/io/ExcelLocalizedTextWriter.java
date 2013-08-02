package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.text.LocalizedText;

import java.io.OutputStream;
import java.util.Collection;

public class ExcelLocalizedTextWriter implements LocalizedTextWriter {

    private OutputStream outputStream;

    public ExcelLocalizedTextWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(Collection<LocalizedText> localizedTexts) {
        //TODO
        throw new RuntimeException("Not yet implemented");
    }

}
