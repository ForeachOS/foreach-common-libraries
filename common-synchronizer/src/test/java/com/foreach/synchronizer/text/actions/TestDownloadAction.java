package com.foreach.synchronizer.text.actions;


import com.foreach.spring.localization.text.LocalizedText;
import com.foreach.spring.localization.text.LocalizedTextService;
import com.foreach.synchronizer.text.io.LocalizedTextFileHandler;
import com.foreach.synchronizer.text.io.LocalizedTextOutputFormat;
import com.foreach.synchronizer.text.io.LocalizedTextWriter;
import com.foreach.synchronizer.text.io.LocalizedTextWriterFactory;
import com.foreach.test.MockedLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = TestDownloadAction.TestConfig.class, loader = MockedLoader.class)
public class TestDownloadAction {

	@Autowired
	private DownloadAction downloadAction;

	@Autowired
	private LocalizedTextWriterFactory localizedTextWriterFactory;

	@Autowired
	private LocalizedTextService localizedTextService;

	@Autowired
	private LocalizedTextFileHandler localizedTextFileHandler;

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
				Mockito.eq( LocalizedTextOutputFormat.XML ), any( OutputStream.class ) ) )
				.thenReturn( localizedTextWriter1, localizedTextWriter2, localizedTextWriter3 );

		when( localizedTextService.getApplications() ).thenReturn( Arrays.asList( "app1", "app2" ) );
		when( localizedTextService.getGroups( "app1" ) ).thenReturn( Arrays.asList( "group11", "group12" ) );
		when( localizedTextService.getGroups( "app2" ) ).thenReturn( Arrays.asList( "group21" ) );

		List<LocalizedText> items11 = new ArrayList<LocalizedText>();
		List<LocalizedText> items12 = new ArrayList<LocalizedText>();
		List<LocalizedText> items21 = new ArrayList<LocalizedText>();
		when( localizedTextService.getLocalizedTextItems( "app1", "group11" ) ).thenReturn( items11 );
		when( localizedTextService.getLocalizedTextItems( "app1", "group12" ) ).thenReturn( items12 );
		when( localizedTextService.getLocalizedTextItems( "app2", "group21" ) ).thenReturn( items21 );


		when( localizedTextFileHandler
				.getOutputStream( outputDirectory, "app1", "group11", LocalizedTextOutputFormat.XML ) ).thenReturn(
				outputStream1 );
		when( localizedTextFileHandler
				.getOutputStream( outputDirectory, "app1", "group12", LocalizedTextOutputFormat.XML ) ).thenReturn(
				outputStream2 );
		when( localizedTextFileHandler
				.getOutputStream( outputDirectory, "app2", "group21", LocalizedTextOutputFormat.XML ) ).thenReturn(
				outputStream3 );

		downloadAction.writeToFiles( outputDirectory );
		verify( localizedTextWriter1, times( 1 ) ).write( items11 );
		verify( localizedTextWriter2, times( 1 ) ).write( items12 );
		verify( localizedTextWriter3, times( 1 ) ).write( items21 );
	}

	public static class TestConfig {
		@Bean
		public DownloadAction downloadAction() {
			return new DownloadAction();
		}
	}
}
