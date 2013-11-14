package com.foreach.synchronizer.text.actions;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public interface SynchronizerAction {
    Options getCliOptions();
    String getActionName();
    void execute( CommandLine commandLine ) throws Exception;
}
