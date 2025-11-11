package ru.itmo.calls.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.itmo.calls.exception.InvalidMessageException;
import ru.itmo.calls.exception.UserOfflineException;
import ru.itmo.calls.model.SignalMessage;
import ru.itmo.calls.model.SignalType;
import ru.itmo.calls.model.UserIdentity;
import ru.itmo.calls.security.AuthHandshakeInterceptor;
import ru.itmo.calls.service.OnlineUsersService;
import ru.itmo.calls.service.SignallingService;
import ru.itmo.calls.service.HeartbeatService;

@Component
public class SignallingWebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger log = LoggerFactory.getLogger(SignallingWebSocketHandler.class);
    
    private final OnlineUsersService onlineUsersService;
    private final SignallingService signallingService;
    private final ObjectMapper objectMapper;
    private final HeartbeatService heartbeatService;
    
    public SignallingWebSocketHandler(
        OnlineUsersService onlineUsersService,
        SignallingService signallingService,
        ObjectMapper objectMapper,
        HeartbeatService heartbeatService
    ) {
        this.onlineUsersService = onlineUsersService;
        this.signallingService = signallingService;
        this.objectMapper = objectMapper;
        this.heartbeatService = heartbeatService;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UserIdentity userIdentity = AuthHandshakeInterceptor.getUserIdentity(session.getAttributes());
        
        if (userIdentity == null || userIdentity.userId() == null) {
            log.error("Connection established without user identity");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        
        Integer userId = userIdentity.userId();

        onlineUsersService.registerUser(userId, session);
        heartbeatService.recordActivity(userId);
        
        log.info("User {} connected. Session: {}", userId, session.getId());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UserIdentity userIdentity = AuthHandshakeInterceptor.getUserIdentity(session.getAttributes());
        if (userIdentity == null || userIdentity.userId() == null) {
            log.error("Received message from unauthenticated session");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        
        Integer userId = userIdentity.userId();
        String payload = message.getPayload();
        
        log.debug("Received message from user {}: {}", userId, payload);
        
        try {
            SignalMessage signalMessage = objectMapper.readValue(payload, SignalMessage.class);
            heartbeatService.recordActivity(userId);

            if (signalMessage.type() == SignalType.PONG) {
                heartbeatService.handlePong(userId);
                return;
            }

            signallingService.routeMessage(signalMessage, userId);
        } catch (InvalidMessageException e) {
            log.warn("Invalid message from user {}: {}", userId, e.getMessage());
            signallingService.sendError(userId, "Invalid message: " + e.getMessage());
            
        } catch (UserOfflineException e) {
            log.warn("Message to offline user from {}: {}", userId, e.getMessage());
            signallingService.sendError(userId, e.getMessage());
            
        } catch (Exception e) {
            log.error("Error handling message from user {}", userId, e);
            signallingService.sendError(userId, "Internal server error");
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UserIdentity userIdentity = AuthHandshakeInterceptor.getUserIdentity(session.getAttributes());
        
        if (userIdentity != null && userIdentity.userId() != null) {
            Integer userId = userIdentity.userId();

            onlineUsersService.unregisterUser(userId);
            heartbeatService.removeUser(userId);
            
            log.info("User {} disconnected. Reason: {}", userId, status);
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        UserIdentity userIdentity = AuthHandshakeInterceptor.getUserIdentity(session.getAttributes());
        Integer userId = userIdentity != null ? userIdentity.userId() : null;
        
        log.error("WebSocket transport error for user {}", userId, exception);
        
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }
}

