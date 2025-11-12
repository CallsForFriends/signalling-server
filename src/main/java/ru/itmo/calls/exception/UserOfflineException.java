package ru.itmo.calls.exception;

import lombok.Getter;

@Getter
public class UserOfflineException extends SignallingException {
    private final Integer userId;
    
    public UserOfflineException(Integer userId) {
        super("User " + userId + " is offline");
        this.userId = userId;
    }
}

