package ru.itmo.calls.exception;

public class UnauthorizedException extends SignallingException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

