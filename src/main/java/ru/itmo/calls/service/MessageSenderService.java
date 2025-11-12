package ru.itmo.calls.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.itmo.calls.exception.UserOfflineException;
import ru.itmo.calls.exception.MessageSendException;
import ru.itmo.calls.model.SignalMessage;

import java.io.IOException;

@Service
public class MessageSenderService {
    private static final Logger log = LoggerFactory.getLogger(MessageSenderService.class);
    
    private final OnlineUsersService onlineUsersService;
    private final ObjectMapper objectMapper;
    
    public MessageSenderService(OnlineUsersService onlineUsersService, ObjectMapper objectMapper) {
        this.onlineUsersService = onlineUsersService;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(Integer userId, SignalMessage message) {
        WebSocketSession session = onlineUsersService.getSession(userId);
        
        if (session == null || !session.isOpen()) {
            throw new UserOfflineException(userId);
        }
        
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
            log.debug("Message sent to user {}: {}", userId, message.type());
        } catch (IOException e) {
            log.error("Failed to send message to user {}", userId, e);
            throw new MessageSendException(userId, e);
        }
    }

    public void sendError(Integer userId, String errorMessage) {
        try {
            SignalMessage errorMsg = SignalMessage.error(errorMessage, userId);
            sendMessage(userId, errorMsg);
        } catch (Exception e) {
            log.error("Failed to send error message to user {}", userId, e);
        }
    }
}
