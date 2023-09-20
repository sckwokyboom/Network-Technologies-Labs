package ru.nsu.fit.sckwo.exceptions;

public class CopyFinderInvalidArgumentsException extends CopyFinderException {
    public CopyFinderInvalidArgumentsException(String message) {
        super(message);
    }

    public CopyFinderInvalidArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }
}