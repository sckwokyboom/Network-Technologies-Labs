package ru.nsu.fit.sckwo.exceptions;

public abstract class CopyFinderException extends RuntimeException {
    public CopyFinderException(String message) {
        super("CopyFinder: " + message);
    }

    public CopyFinderException(Throwable t) {
        super(t);
    }

    public CopyFinderException(String message, Throwable cause) {
        super("CopyFinder: " + message, cause);
    }
}
