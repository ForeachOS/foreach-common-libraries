package com.foreach.synchronizer.text;

import com.foreach.synchronizer.text.actions.SynchronizerAction;
import org.apache.commons.cli.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public class TextSynchronizer implements ApplicationContextAware {
	private ApplicationContext context;

	public void execute( String[] args ) {
		Map<String, SynchronizerAction> actions = context.getBeansOfType( SynchronizerAction.class );

		SynchronizerAction foundAction = null;

		for ( SynchronizerAction action : actions.values() ) {
			if ( action.getActionName().equals( args[ 1 ] ) ) {
				foundAction = action;
				break;
			}
		}

		if ( foundAction == null ) {
			throw new TextSynchronizerException( "Unknown action:" + args[ 1 ] );
		}

		CommandLineParser parser = new PosixParser();
		try {
			CommandLine cmd = parser.parse( foundAction.getCliOptions(), args );
			foundAction.execute( cmd );
		} catch ( ParseException e ) {
			throw new TextSynchronizerException( e.getMessage(), e );
		}
	}

	public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
		this.context = applicationContext;
	}
}
