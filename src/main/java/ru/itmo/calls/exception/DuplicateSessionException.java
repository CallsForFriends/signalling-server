package ru.itmo.calls.exception;

import lombok.Getter;

@Getter
public class DuplicateSessionException extends SignallingException {
    private final Integer userId;
    
    public DuplicateSessionException(Integer userId) {
        super("User " + userId + " already has an active session");
        this.userId = userId;
    }
}
