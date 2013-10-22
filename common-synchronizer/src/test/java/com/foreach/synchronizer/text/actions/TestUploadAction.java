package com.foreach.synchronizer.text.actions;

import com.foreach.spring.localization.Language;
import com.foreach.spring.localization.LanguageConfigurator;
import com.foreach.spring.localization.text.LocalizedText;
import com.foreach.spring.localization.text.LocalizedTextFields;
import com.foreach.spring.localization.text.LocalizedTextService;
import com.foreach.synchronizer.text.io.*;
import com.foreach.test.MockedLoader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith( SpringJUnit4ClassRunner.class )
@DirtiesContext( classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD )
@ContextConfiguration( classes = TestUploadAction.TestConfig.class, loader = MockedLoader.class )
public class TestUploadAction {

    @Autowired
    private UploadAction uploadAction;

    @Autowired
    private LocalizedTextReaderFactory localizedTextReaderFactory;

    @Autowired
    private LocalizedTextService localizedTextService;

    @Autowired
    private LocalizedTextFileHandler localizedTextFileHandler;

    @Before
    public void setup() {
        LanguageConfigurator languageConfigurator = new LanguageConfigurator( TestLanguage.class );
    }

    @Test
    public void testUpload() throws Exception {
        String inputDirectory = "/some/dir";
        InputStream inputStream = mock( InputStream.class );
        List<InputStream> inputStreams = new ArrayList<InputStream>();
        inputStreams.add( inputStream );
        LocalizedTextReader localizedTextReader = mock( LocalizedTextReader.class );
        when( localizedTextFileHandler
                .getInputStreams( inputDirectory ) ).thenReturn( inputStreams );
        when( localizedTextReaderFactory.createLocalizedTextReader(
                eq( LocalizedTextFormat.XML ), any( InputStream.class ) ) )
                .thenReturn( localizedTextReader );

        Collection<LocalizedText> localizedTexts = new ArrayList<LocalizedText>();
        LocalizedText localizedText = getLocalizedText();
        localizedTexts.add( localizedText );
        when( localizedTextReader.read() ).thenReturn( localizedTexts );

        uploadAction.upload( inputDirectory, LocalizedTextFormat.XML );

        verify( localizedTextService, times( 1 ) ).saveLocalizedText( localizedText );
    }

    @Test
    public void testExcelFormat() throws ParseException {
        verifyExpectedFormatUsed( new String[]{"-i", "/some/dir/", "-f", "EXCEL"}, LocalizedTextFormat.EXCEL );
    }

    @Test
    public void testExcelFormatLowerCase() throws ParseException {
        verifyExpectedFormatUsed( new String[]{"-i", "/some/dir/", "-f", "excel"}, LocalizedTextFormat.EXCEL );
    }

    @Test
    public void testXMLFormat() throws ParseException {
        verifyExpectedFormatUsed( new String[]{"-i", "/some/dir/", "-f", "XML"}, LocalizedTextFormat.XML );
    }

    @Test
    public void testDefaultFormat() throws ParseException {
        verifyExpectedFormatUsed( new String[]{"-i", "/some/dir/"}, LocalizedTextFormat.XML );
    }

    private void verifyExpectedFormatUsed( String[] args, LocalizedTextFormat expectedOutputFormat ) throws ParseException {
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = parser.parse( uploadAction.getCliOptions(), args );

        List<InputStream> inputStreams = new ArrayList<InputStream>();
        InputStream inputStream = mock( InputStream.class );
        inputStreams.add( inputStream );

        when( localizedTextFileHandler.getInputStreams( "/some/dir/" ) ).thenReturn( inputStreams );

        LocalizedTextReader localizedTextReader = mock( LocalizedTextReader.class );
        when( localizedTextReaderFactory.createLocalizedTextReader( any( LocalizedTextFormat.class ), any( InputStream.class ) ) ).thenReturn( localizedTextReader );
        uploadAction.execute( commandLine );

        verify( localizedTextReaderFactory ).createLocalizedTextReader( eq( expectedOutputFormat ), any( InputStream.class ) );
    }

    public static class TestConfig {
        @Bean
        public UploadAction uploadAction() {
            return new UploadAction();
        }
    }

    private LocalizedText getLocalizedText() {
        LocalizedText localizedText = new LocalizedText();
        localizedText.setApplication( "application1" );
        localizedText.setGroup( "group1" );
        localizedText.setLabel( "label1" );
        localizedText.setUpdated( new Date() );
        Language nl = TestLanguage.NL;
        LocalizedTextFields localizedTextFieldsNl = new LocalizedTextFields( nl );
        localizedTextFieldsNl.setText( "NL text1" );
        localizedText.addFields( localizedTextFieldsNl );
        Language fr = TestLanguage.FR;
        LocalizedTextFields localizedTextFieldsFr = new LocalizedTextFields( fr );
        localizedTextFieldsFr.setText( "FR text1" );
        localizedText.addFields( localizedTextFieldsFr );
        return localizedText;
    }
}
