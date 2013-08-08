package com.foreach.synchronizer.text;

import com.foreach.synchronizer.text.actions.DownloadAction;
import com.foreach.synchronizer.text.actions.SynchronizerAction;
import com.foreach.test.MockedLoader;
import org.apache.commons.cli.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = TestTextSynchronizer.TestConfig.class, loader = MockedLoader.class)
public class TestTextSynchronizer {
    @Autowired
    private TextSynchronizer textSynchronizer;

    @Autowired
    private SynchronizerAction downloadAction;

    @Autowired
    private SynchronizerAction mergeAction;

    @Test
    public void execute() throws ParseException {
        String downloadActionName = "download";

        String[] args = {downloadActionName, "--output-dir=path/to/files/production"};

        Options options = new Options();
        options.addOption(DownloadAction.SHORT_OPTION_OUTPUT_DIR, DownloadAction.OPTION_OUTPUT_DIR, true, "the output directory to save the files to");
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, args);

        when(mergeAction.getActionName()).thenReturn("merge");
        when(downloadAction.getActionName()).thenReturn(downloadActionName);
        when(downloadAction.getCliOptions()).thenReturn(options);

        textSynchronizer.execute(args);

        ArgumentCaptor<CommandLine> argument = ArgumentCaptor.forClass(CommandLine.class);
        verify(downloadAction, times(1)).execute(argument.capture());
        assertArrayEquals(cmd.getOptions(), argument.getValue().getOptions());
        assertArrayEquals(cmd.getArgs(), argument.getValue().getArgs());
        for (int i = 0; i < argument.getValue().getOptions().length; i++) {
            assertEquals(cmd.getOptions()[i].getValue(), argument.getValue().getOptions()[i].getValue());
        }
    }

    @Test
    public void executeEmptyArguments() throws ParseException {
        String[] args = {};
        boolean exceptionThrown = false;

        try {
            textSynchronizer.execute(args);
        } catch (TextSynchronizerException ex) {
            assertEquals("Argument is null or empty", ex.getMessage());
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void executeMissingArgument() {
        String downloadActionName = "download";
        when(downloadAction.getActionName()).thenReturn(downloadActionName);

        Option option = new Option(DownloadAction.SHORT_OPTION_OUTPUT_DIR, DownloadAction.OPTION_OUTPUT_DIR, true, "the output directory to save the files to");
        option.setRequired(true);

        Options options = new Options();
        options.addOption(option);
        when(downloadAction.getCliOptions()).thenReturn(options);

        boolean thrown = false;
        String[] args = {downloadActionName};
        try {
            textSynchronizer.execute(args);
        } catch (TextSynchronizerException e) {
            assertEquals("Missing required option: o", e.getMessage());
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void executeWrongArgument() {
        String downloadActionName = "download";
        when(downloadAction.getActionName()).thenReturn(downloadActionName);

        Options options = new Options();
        options.addOption(DownloadAction.SHORT_OPTION_OUTPUT_DIR, DownloadAction.OPTION_OUTPUT_DIR, true, "the output directory to save the files to");
        when(downloadAction.getCliOptions()).thenReturn(options);

        boolean thrown = false;
        String[] args = {downloadActionName, "--wrong-option=blah"};
        try {
            textSynchronizer.execute(args);
        } catch (TextSynchronizerException e) {
            assertEquals("Unrecognized option: --wrong-option=blah", e.getMessage());
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void executeWrongCommand() throws TextSynchronizerException {
        String[] args = {"unknownAction", "--output-dir=path/to/files/production"};
        boolean thrown = false;
        try {
            textSynchronizer.execute(args);
        } catch (TextSynchronizerException e) {
            assertEquals("Unknown action: unknownAction", e.getMessage());
            thrown = true;
        }
        assertTrue(thrown);
    }

    public static class TestConfig {
        @Bean
        public TextSynchronizer textSynchronizer() {
            return new TextSynchronizer();
        }

        @Bean
        public SynchronizerAction mergeAction() {
            return mock(SynchronizerAction.class);
        }

        @Bean
        public SynchronizerAction downloadAction() {
            return mock(SynchronizerAction.class);
        }
    }
}
