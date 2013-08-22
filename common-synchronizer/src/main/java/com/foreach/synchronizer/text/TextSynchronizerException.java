package com.foreach.synchronizer.text;

public class TextSynchronizerException extends RuntimeException {
    public TextSynchronizerException() {
    }

    public TextSynchronizerException( String message ) {
        super( message );
    }

    public TextSynchronizerException( String message, Throwable cause ) {
        super( message, cause );
    }

    public TextSynchronizerException( Throwable cause ) {
        super( cause );
    }
}
