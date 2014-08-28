package com.foreach.synchronizer.text.io;

import com.foreach.common.spring.localization.Language;
import com.foreach.common.spring.localization.LanguageConfigurator;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestExcelLocalizedTextWriter extends BaseLocalizedTextWriterTest {

    @Test
    public void testWrite() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExcelLocalizedTextWriter localizedTextWriter = new ExcelLocalizedTextWriter( outputStream );
        localizedTextWriter.write( createLocalizedTexts() );
        String actualOutput = new String( outputStream.toByteArray() );
        System.out.println( actualOutput );
        assertFalse( StringUtils.isEmpty( actualOutput ) );

        //We don't want to test the XML structure itself since this is waaay to complex, but we'll
        //test that all content that should be in the file is actually present.

        assertTrue( actualOutput.contains( BaseLocalizedTextWriterTest.APP ) );
        assertTrue( actualOutput.contains( BaseLocalizedTextWriterTest.GROUP ) );
        assertTrue( actualOutput.contains( BaseLocalizedTextWriterTest.LABEL ) );
        assertTrue( actualOutput.contains( BaseLocalizedTextWriterTest.TEXT_EN ) );
        assertTrue( actualOutput.contains( BaseLocalizedTextWriterTest.TEXT_NL ) );
        for( Language language : LanguageConfigurator.getLanguages() ) {
            assertTrue( "Language " + language + " should be present in the output " + actualOutput, actualOutput.contains( language.getName() ) );
        }
    }
}
