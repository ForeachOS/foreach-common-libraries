package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.LanguageConfigurator;
import com.foreach.test.MockedLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith( SpringJUnit4ClassRunner.class )
@DirtiesContext( classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD )
@ContextConfiguration( classes = TestLocalizedTextWriterFactoryImpl.TestConfig.class, loader = MockedLoader.class )
public class TestLocalizedTextWriterFactoryImpl {

	@Autowired
	private LocalizedTextWriterFactory localizedTextWriterFactory;

    @Before
    public void setup() {
        new LanguageConfigurator(TestLanguage.class);
    }

    @Test
	public void createLocalizedTextWriter() throws Exception {
		OutputStream outputStream = mock( OutputStream.class );
		//Xml output format
		LocalizedTextWriter xmlWriter = localizedTextWriterFactory.createLocalizedTextWriter( LocalizedTextOutputFormat.XML, outputStream );
		assertNotNull( xmlWriter );
		assertEquals( xmlWriter.getClass(), XmlLocalizedTextWriter.class );
	}

	@Test
	public void createLocalizedTextWriterExcel() throws Exception {
		OutputStream outputStream = mock( OutputStream.class );
		LocalizedTextWriter excelWriter = localizedTextWriterFactory.createLocalizedTextWriter( LocalizedTextOutputFormat.EXCEL, outputStream );
		assertNotNull( excelWriter );
		assertEquals( excelWriter.getClass(), ExcelLocalizedTextWriter.class );
	}

	public static class TestConfig {

		@Bean
		public LocalizedTextWriterFactory localizedTextWriterFactory() {
			return new LocalizedTextWriterFactoryImpl();
		}
	}
}
