package ru.itmo.calls.exception;

import lombok.Getter;

@Getter
public class MessageSendException extends SignallingException {
    private final Integer userId;
    
    public MessageSendException(Integer userId, Throwable cause) {
        super("Failed to send message to user " + userId, cause);
        this.userId = userId;
    }
}
