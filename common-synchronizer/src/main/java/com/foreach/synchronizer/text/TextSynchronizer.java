package com.foreach.synchronizer.text;

import com.foreach.synchronizer.text.actions.SynchronizerAction;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TextSynchronizer {

    @Autowired
    private java.util.Collection<SynchronizerAction> synchronizerActions;

    public void execute(String[] args) {
        if (ArrayUtils.isEmpty(args)) {
            throw new TextSynchronizerException(new IllegalArgumentException("Argument is null or empty"));
        }
        SynchronizerAction foundAction = getSynchronizerActionForName(args[0]);

        if (foundAction == null) {
            throw new TextSynchronizerException("Unknown action: " + args[0]);
        }

        CommandLine cmd = parseArguments(args, foundAction);

        foundAction.execute(cmd);
    }

    private CommandLine parseArguments(String[] args, SynchronizerAction foundAction) {
        try {
            CommandLineParser parser = new PosixParser();
            return parser.parse(foundAction.getCliOptions(), args);
        } catch (ParseException e) {
            throw new TextSynchronizerException(e.getMessage(), e);
        }
    }

    private SynchronizerAction getSynchronizerActionForName(String actionName) {
        for (SynchronizerAction action : synchronizerActions) {
            if (StringUtils.equalsIgnoreCase(action.getActionName(), actionName)) {
                return action;
            }
        }
        return null;
    }
}
