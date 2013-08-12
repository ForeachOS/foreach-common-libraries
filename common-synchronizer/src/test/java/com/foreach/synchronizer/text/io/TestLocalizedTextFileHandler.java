package com.foreach.synchronizer.text.io;

import com.foreach.test.MockedLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = TestLocalizedTextFileHandler.TestConfig.class, loader = MockedLoader.class)
public class TestLocalizedTextFileHandler {

    @Autowired
    private LocalizedTextFileHandlerImpl localizedTextFileHandler;

    private String outputDir = "/test_files/";
    private String application = "my_application";
    private String group = "my_group";


    @Test
    public void testGetOutputStream() throws Exception {
        String expectedContent = "test test";
        OutputStream outputStream = localizedTextFileHandler.getOutputStream(outputDir, application, group, LocalizedTextOutputFormat.XML);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(expectedContent);
        writer.close();
        File expectedFile = new File(outputDir + application + "." + group + ".xml");
        assertTrue(expectedFile.exists());
        assertTrue(expectedFile.isFile());
        InputStream fileInputStream = new FileInputStream(expectedFile);
        String actualContent = IOUtils.toString(fileInputStream);
        assertEquals(expectedContent, actualContent);
        fileInputStream.close();
    }

    @After
    public void removeTestFile() throws IOException {
        File outputFile = new File(outputDir + application + "." + group + ".xml");
        if (outputFile.exists()) {
            outputFile.delete();
        }
        File outputDirectory = new File(outputDir);
        if (outputDirectory.exists()) {
            FileUtils.deleteDirectory(outputDirectory);
        }
    }

    public static class TestConfig {
        @Bean
        public LocalizedTextFileHandler localizedTextFileHandler() {
            return new LocalizedTextFileHandlerImpl();
        }
    }
}
