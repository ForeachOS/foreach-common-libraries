package com.foreach.synchronizer.text.actions;

import com.foreach.common.spring.localization.Language;
import com.foreach.common.spring.localization.LanguageConfigurator;
import com.foreach.common.spring.localization.text.LocalizedText;
import com.foreach.common.spring.localization.text.LocalizedTextFields;
import com.foreach.common.test.MockedLoader;
import com.foreach.synchronizer.text.io.*;
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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = TestMergeAction.TestConfig.class, loader = MockedLoader.class)
public class TestMergeAction
{

	@Autowired
	private MergeAction mergeAction;

	@Autowired
	private LocalizedTextWriterFactory localizedTextWriterFactory;

	@Autowired
	private LocalizedTextReaderFactory localizedTextReaderFactory;

	@Autowired
	private LocalizedTextFileHandler localizedTextFileHandler;

	@Before
	public void setup() {
		LanguageConfigurator languageConfigurator = new LanguageConfigurator( TestLanguage.class );
	}

	@Test
	public void testWriteToFiles() throws Exception {

		String outputDir = "/someDir";
		String[] inputDirs = new String[2];
		inputDirs[0] = "/firstInputDir";
		inputDirs[1] = "/secondInputDir";
		LocalizedTextFormat format = LocalizedTextFormat.XML;

		InputStream inputStream1 = mock( InputStream.class );
		InputStream inputStream2 = mock( InputStream.class );
		InputStream inputStream3 = mock( InputStream.class );

		LocalizedTextReader localizedTextReader1 = mock( LocalizedTextReader.class );
		LocalizedTextReader localizedTextReader2 = mock( LocalizedTextReader.class );
		LocalizedTextReader localizedTextReader3 = mock( LocalizedTextReader.class );

		List<InputStream> inputStreamList12 = new ArrayList<InputStream>();
		inputStreamList12.add( inputStream1 );
		inputStreamList12.add( inputStream2 );
		List<InputStream> inputStreamList3 = new ArrayList<InputStream>();
		inputStreamList12.add( inputStream3 );

		LocalizedText localizedText1 = getLocalizedText1();
		LocalizedText localizedText2 = getLocalizedText2();
		LocalizedText localizedText3 = getLocalizedText3();
		List<LocalizedText> localizedTexts1 = new ArrayList<LocalizedText>();
		List<LocalizedText> localizedTexts2 = new ArrayList<LocalizedText>();
		List<LocalizedText> localizedTexts3 = new ArrayList<LocalizedText>();
		localizedTexts1.add( localizedText1 );
		localizedTexts2.add( localizedText2 );
		localizedTexts3.add( localizedText3 );

		when( localizedTextFileHandler.getInputStreams( inputDirs[0] ) ).thenReturn( inputStreamList12 );
		when( localizedTextFileHandler.getInputStreams( inputDirs[1] ) ).thenReturn( inputStreamList3 );

		when( localizedTextReaderFactory.createLocalizedTextReader( LocalizedTextFormat.XML, inputStream1 ) )
				.thenReturn( localizedTextReader1 );
		when( localizedTextReaderFactory.createLocalizedTextReader( LocalizedTextFormat.XML, inputStream2 ) )
				.thenReturn( localizedTextReader2 );
		when( localizedTextReaderFactory.createLocalizedTextReader( LocalizedTextFormat.XML, inputStream3 ) )
				.thenReturn( localizedTextReader3 );

		when( localizedTextReader1.read() ).thenReturn( localizedTexts1 );
		when( localizedTextReader2.read() ).thenReturn( localizedTexts2 );
		when( localizedTextReader3.read() ).thenReturn( localizedTexts3 );

		OutputStream outputStream1 = mock( OutputStream.class );
		OutputStream outputStream2 = mock( OutputStream.class );
		OutputStream outputStream3 = mock( OutputStream.class );

		LocalizedTextWriter localizedTextWriter1 = mock( LocalizedTextWriter.class );
		LocalizedTextWriter localizedTextWriter2 = mock( LocalizedTextWriter.class );
		LocalizedTextWriter localizedTextWriter3 = mock( LocalizedTextWriter.class );

		when( localizedTextFileHandler.getOutputStream( outputDir, "application1", "group1", format ) ).thenReturn(
				outputStream1 );
		when( localizedTextFileHandler.getOutputStream( outputDir, "application2", "group2", format ) ).thenReturn(
				outputStream2 );
		when( localizedTextFileHandler.getOutputStream( outputDir, "application3", "group3", format ) ).thenReturn(
				outputStream3 );

		when( localizedTextWriterFactory.createLocalizedTextWriter( format, outputStream1 ) ).thenReturn(
				localizedTextWriter1 );
		when( localizedTextWriterFactory.createLocalizedTextWriter( format, outputStream2 ) ).thenReturn(
				localizedTextWriter2 );
		when( localizedTextWriterFactory.createLocalizedTextWriter( format, outputStream3 ) ).thenReturn(
				localizedTextWriter3 );

		mergeAction.merge( outputDir, inputDirs, format );

		verify( localizedTextWriter1, times( 1 ) ).write( localizedTexts1 );
		verify( localizedTextWriter2, times( 1 ) ).write( localizedTexts2 );
		verify( localizedTextWriter3, times( 1 ) ).write( localizedTexts3 );
	}

	@Test
	public void testExcelFormat() throws ParseException {
		verifyExpectedFormatUsed(
				new String[] { "-i", "/inputDir", "-i", "/another/inputDir", "-o", "/outputDir", "-f", "EXCEL" },
				LocalizedTextFormat.EXCEL );
	}

	@Test
	public void testExcelFormatLowerCase() throws ParseException {
		verifyExpectedFormatUsed(
				new String[] { "-i", "/inputDir", "-i", "/another/inputDir", "-o", "/outputDir", "-f", "excel" },
				LocalizedTextFormat.EXCEL );
	}

	@Test
	public void testXMLFormat() throws ParseException {
		verifyExpectedFormatUsed(
				new String[] { "-i", "/inputDir", "-i", "/another/inputDir", "-o", "/outputDir", "-f", "XML" },
				LocalizedTextFormat.XML );
	}

	@Test
	public void testDefaultFormat() throws ParseException {
		verifyExpectedFormatUsed( new String[] { "-i", "/inputDir", "-i", "/another/inputDir", "-o", "/outputDir" },
		                          LocalizedTextFormat.XML );
	}

	private void verifyExpectedFormatUsed( String[] args,
	                                       LocalizedTextFormat expectedOutputFormat ) throws ParseException {

		CommandLineParser parser = new PosixParser();
		CommandLine commandLine = parser.parse( mergeAction.getCliOptions(), args );

		InputStream inputStream1 = mock( InputStream.class );
		InputStream inputStream2 = mock( InputStream.class );
		LocalizedTextReader localizedTextReader1 = mock( LocalizedTextReader.class );
		LocalizedTextReader localizedTextReader2 = mock( LocalizedTextReader.class );
		List<InputStream> inputStreamList1 = new ArrayList<InputStream>();
		List<InputStream> inputStreamList2 = new ArrayList<InputStream>();
		inputStreamList1.add( inputStream1 );
		inputStreamList2.add( inputStream2 );
		LocalizedText localizedText1 = getLocalizedText1();
		LocalizedText localizedText2 = getLocalizedText2();
		List<LocalizedText> localizedTexts1 = new ArrayList<LocalizedText>();
		List<LocalizedText> localizedTexts2 = new ArrayList<LocalizedText>();
		localizedTexts1.add( localizedText1 );
		localizedTexts2.add( localizedText2 );

		when( localizedTextFileHandler.getInputStreams( "/inputDir" ) ).thenReturn( inputStreamList1 );
		when( localizedTextFileHandler.getInputStreams( "/another/inputDir" ) ).thenReturn( inputStreamList2 );

		when( localizedTextReaderFactory.createLocalizedTextReader( expectedOutputFormat, inputStream1 ) )
				.thenReturn( localizedTextReader1 );
		when( localizedTextReaderFactory.createLocalizedTextReader( expectedOutputFormat, inputStream2 ) )
				.thenReturn( localizedTextReader2 );

		when( localizedTextReader1.read() ).thenReturn( localizedTexts1 );
		when( localizedTextReader2.read() ).thenReturn( localizedTexts2 );

		LocalizedTextWriter localizedTextWriter = mock( LocalizedTextWriter.class );

		when( localizedTextWriterFactory.createLocalizedTextWriter( eq( expectedOutputFormat ),
		                                                            any( OutputStream.class ) ) ).thenReturn(
				localizedTextWriter );

		mergeAction.execute( commandLine );

		verify( localizedTextFileHandler ).getOutputStream( "/outputDir", "application1", "group1",
		                                                    expectedOutputFormat );
		verify( localizedTextWriterFactory, times( 2 ) ).createLocalizedTextWriter( eq( expectedOutputFormat ),
		                                                                            any( OutputStream.class ) );
	}

	public static class TestConfig
	{
		@Bean
		public MergeAction mergeAction() {
			return new MergeAction();
		}
	}

	private LocalizedText getLocalizedText1() {
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

	private LocalizedText getLocalizedText2() {
		LocalizedText localizedText = new LocalizedText();
		localizedText.setApplication( "application2" );
		localizedText.setGroup( "group2" );
		localizedText.setLabel( "label2" );
		localizedText.setUpdated( new Date() );
		Language nl = TestLanguage.NL;
		LocalizedTextFields localizedTextFieldsNl = new LocalizedTextFields( nl );
		localizedTextFieldsNl.setText( "NL text2" );
		localizedText.addFields( localizedTextFieldsNl );
		Language fr = TestLanguage.FR;
		LocalizedTextFields localizedTextFieldsFr = new LocalizedTextFields( fr );
		localizedTextFieldsFr.setText( "FR text2" );
		localizedText.addFields( localizedTextFieldsFr );
		return localizedText;
	}

	private LocalizedText getLocalizedText3() {
		LocalizedText localizedText = new LocalizedText();
		localizedText.setApplication( "application3" );
		localizedText.setGroup( "group3" );
		localizedText.setLabel( "label3" );
		localizedText.setUpdated( new Date() );
		Language nl = TestLanguage.NL;
		LocalizedTextFields localizedTextFieldsNl = new LocalizedTextFields( nl );
		localizedTextFieldsNl.setText( "NL text3" );
		localizedText.addFields( localizedTextFieldsNl );
		Language fr = TestLanguage.FR;
		LocalizedTextFields localizedTextFieldsFr = new LocalizedTextFields( fr );
		localizedTextFieldsFr.setText( "FR text3" );
		localizedText.addFields( localizedTextFieldsFr );
		return localizedText;
	}
}
