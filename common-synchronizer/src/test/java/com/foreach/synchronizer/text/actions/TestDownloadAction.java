package com.foreach.synchronizer.text.actions;

import com.foreach.spring.localization.LanguageConfigurator;
import com.foreach.spring.localization.text.LocalizedText;
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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith( SpringJUnit4ClassRunner.class )
@DirtiesContext( classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD )
@ContextConfiguration( classes = TestDownloadAction.TestConfig.class, loader = MockedLoader.class )
public class TestDownloadAction {

    @Autowired
    private DownloadAction downloadAction;

    @Autowired
    private LocalizedTextWriterFactory localizedTextWriterFactory;

    @Autowired
    private LocalizedTextService localizedTextService;

    @Autowired
    private LocalizedTextFileHandler localizedTextFileHandler;

    @Before
    public void setup() {
        LanguageConfigurator languageConfigurator = new LanguageConfigurator( TestLanguage.class );
    }

    @Test
    public void testWriteToFiles() throws Exception {
        String outputDirectory = "/some/dir";
        OutputStream outputStream1 = mock( OutputStream.class );
        OutputStream outputStream2 = mock( OutputStream.class );
        OutputStream outputStream3 = mock( OutputStream.class );

        LocalizedTextWriter localizedTextWriter1 = mock( LocalizedTextWriter.class );
        LocalizedTextWriter localizedTextWriter2 = mock( LocalizedTextWriter.class );
        LocalizedTextWriter localizedTextWriter3 = mock( LocalizedTextWriter.class );
        when( localizedTextWriterFactory.createLocalizedTextWriter(
                eq( LocalizedTextFormat.XML ), any( OutputStream.class ) ) )
                .thenReturn( localizedTextWriter1, localizedTextWriter2, localizedTextWriter3 );

        when( localizedTextService.getApplications() ).thenReturn( Arrays.asList( "app1", "app2" ) );
        when( localizedTextService.getGroups( "app1" ) ).thenReturn( Arrays.asList( "group11", "group12" ) );
        when( localizedTextService.getGroups( "app2" ) ).thenReturn( Arrays.asList( "group21" ) );

        List<LocalizedText> items11 = new ArrayList<LocalizedText>();
        items11.add( new LocalizedText() );
        List<LocalizedText> items12 = new ArrayList<LocalizedText>();
        items12.add( new LocalizedText() );
        List<LocalizedText> items21 = new ArrayList<LocalizedText>();
        items21.add( new LocalizedText() );

        when( localizedTextService.getLocalizedTextItems( "app1", "group11" ) ).thenReturn( items11 );
        when( localizedTextService.getLocalizedTextItems( "app1", "group12" ) ).thenReturn( items12 );
        when( localizedTextService.getLocalizedTextItems( "app2", "group21" ) ).thenReturn( items21 );

        when( localizedTextFileHandler
                .getOutputStream( outputDirectory, "app1", "group11", LocalizedTextFormat.XML ) ).thenReturn(
                outputStream1 );
        when( localizedTextFileHandler
                .getOutputStream( outputDirectory, "app1", "group12", LocalizedTextFormat.XML ) ).thenReturn(
                outputStream2 );
        when( localizedTextFileHandler
                .getOutputStream( outputDirectory, "app2", "group21", LocalizedTextFormat.XML ) ).thenReturn(
                outputStream3 );

        downloadAction.writeToFiles( outputDirectory, LocalizedTextFormat.XML );
        verify( localizedTextWriter1, times( 1 ) ).write( items11 );
        verify( localizedTextWriter2, times( 1 ) ).write( items12 );
        verify( localizedTextWriter3, times( 1 ) ).write( items21 );
    }

    @Test
    public void testExcelFormat() throws ParseException {
        verifyExpectedFormatUsed( new String[]{"-o", "/some/dir/", "-f", "EXCEL"}, LocalizedTextFormat.EXCEL );
    }

    @Test
    public void testExcelFormatLowerCase() throws ParseException {
        verifyExpectedFormatUsed( new String[]{"-o", "/some/dir/", "-f", "excel"}, LocalizedTextFormat.EXCEL );
    }

    @Test
    public void testXMLFormat() throws ParseException {
        verifyExpectedFormatUsed( new String[]{"-o", "/some/dir/", "-f", "XML"}, LocalizedTextFormat.XML );
    }

    @Test
    public void testDefaultFormat() throws ParseException {
        verifyExpectedFormatUsed( new String[]{"-o", "/some/dir/"}, LocalizedTextFormat.XML );
    }

    private void verifyExpectedFormatUsed( String[] args, LocalizedTextFormat expectedOutputFormat ) throws ParseException {
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = parser.parse( downloadAction.getCliOptions(), args );

        when( localizedTextService.getApplications() ).thenReturn( Arrays.asList( "app3" ) );
        when( localizedTextService.getGroups( "app3" ) ).thenReturn( Arrays.asList( "group31" ) );
        List<LocalizedText> localizedTexts = new ArrayList<LocalizedText>(  );
        LocalizedText localizedText = new LocalizedText();
        localizedText.setApplication( "app3" );
        localizedTexts.add(localizedText);
        when( localizedTextService.getLocalizedTextItems( "app3", "group31" ) ).thenReturn(localizedTexts);
        LocalizedTextWriter localizedTextWriter =  mock( LocalizedTextWriter.class );
        when( localizedTextWriterFactory.createLocalizedTextWriter( eq( expectedOutputFormat ), any( OutputStream.class ) ) ).thenReturn( localizedTextWriter );

        downloadAction.execute( commandLine );

        verify( localizedTextFileHandler ).getOutputStream( "/some/dir/", "app3", "group31", expectedOutputFormat );
        verify( localizedTextWriterFactory ).createLocalizedTextWriter( eq( expectedOutputFormat ), any( OutputStream.class ) );
    }

    public static class TestConfig {
        @Bean
        public DownloadAction downloadAction() {
            return new DownloadAction();
        }
    }
}
