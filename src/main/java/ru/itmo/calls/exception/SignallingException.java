package ru.itmo.calls.exception;

public class SignallingException extends RuntimeException {
    public SignallingException(String message) {
        super(message);
    }
    
    public SignallingException(String message, Throwable cause) {
        super(message, cause);
    }
}

