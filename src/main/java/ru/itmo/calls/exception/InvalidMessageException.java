package ru.itmo.calls.exception;

public class InvalidMessageException extends SignallingException {
    public InvalidMessageException(String message) {
        super(message);
    }
    
    public InvalidMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}

