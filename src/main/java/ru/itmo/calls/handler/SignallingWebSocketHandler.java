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
import ru.itmo.calls.security.AuthProvider;

@Component
public class SignallingWebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger log = LoggerFactory.getLogger(SignallingWebSocketHandler.class);
    
    private final OnlineUsersService onlineUsersService;
    private final SignallingService signallingService;
    private final ObjectMapper objectMapper;
    private final HeartbeatService heartbeatService;
    private final AuthProvider authProvider;
    
    public SignallingWebSocketHandler(
        OnlineUsersService onlineUsersService,
        SignallingService signallingService,
        ObjectMapper objectMapper,
        HeartbeatService heartbeatService,
        AuthProvider authProvider
    ) {
        this.onlineUsersService = onlineUsersService;
        this.signallingService = signallingService;
        this.objectMapper = objectMapper;
        this.heartbeatService = heartbeatService;
        this.authProvider = authProvider;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        if (AuthHandshakeInterceptor.isAuthenticated(session.getAttributes())) {
            UserIdentity userIdentity = AuthHandshakeInterceptor.getUserIdentity(session.getAttributes());
            Integer userId = userIdentity.userId();
            
            onlineUsersService.registerUser(userId, session);
            heartbeatService.recordActivity(userId);
            
            log.info("User {} connected via header auth. Session: {}", userId, session.getId());
        } else {
            log.info("WebSocket connected, waiting for AUTH message. Session: {}", session.getId());
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        try {
            SignalMessage signalMessage = objectMapper.readValue(payload, SignalMessage.class);
            if (signalMessage.type() == SignalType.AUTH) {
                handleAuthMessage(session, signalMessage);
                return;
            }

            UserIdentity userIdentity = AuthHandshakeInterceptor.getUserIdentity(session.getAttributes());
            if (userIdentity == null || userIdentity.userId() == null) {
                log.warn("Received non-AUTH message from unauthenticated session");
                sendAuthFailed(session, "Authentication required");
                return;
            }
            
            Integer userId = userIdentity.userId();
            log.debug("Received message from user {}: {}", userId, payload);
            
            heartbeatService.recordActivity(userId);

            if (signalMessage.type() == SignalType.PONG) {
                heartbeatService.handlePong(userId);
                return;
            }

            signallingService.routeMessage(signalMessage, userId);
        } catch (InvalidMessageException e) {
            log.warn("Invalid message: {}", e.getMessage());
            sendErrorToSession(session, "Invalid message: " + e.getMessage());
            
        } catch (UserOfflineException e) {
            log.warn("Message to offline user: {}", e.getMessage());
            sendErrorToSession(session, e.getMessage());
            
        } catch (Exception e) {
            log.error("Error handling message", e);
            sendErrorToSession(session, "Internal server error");
        }
    }
    
    private void handleAuthMessage(WebSocketSession session, SignalMessage authMessage) {
        try {
            if (authMessage.payload() == null || !authMessage.payload().has("token")) {
                sendAuthFailed(session, "Token is required");
                return;
            }
            
            String token = authMessage.payload().get("token").asText();
            UserIdentity userIdentity = authProvider.validateToken(token);
            
            if (userIdentity == null || userIdentity.userId() == null) {
                sendAuthFailed(session, "Invalid token");
                return;
            }

            AuthHandshakeInterceptor.setUserIdentity(session.getAttributes(), userIdentity);
            onlineUsersService.registerUser(userIdentity.userId(), session);
            heartbeatService.recordActivity(userIdentity.userId());
            
            sendAuthSuccess(session);
            log.info("User {} authenticated via message. Session: {}", userIdentity.userId(), session.getId());
            
        } catch (Exception e) {
            log.error("Error during message-based authentication", e);
            sendAuthFailed(session, "Authentication error");
        }
    }
    
    private void sendAuthSuccess(WebSocketSession session) {
        try {
            SignalMessage response = new SignalMessage(SignalType.AUTH_SUCCESS, null, null, null);
            String jsonMessage = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            log.error("Failed to send AUTH_SUCCESS", e);
        }
    }
    
    private void sendAuthFailed(WebSocketSession session, String reason) {
        try {
            SignalMessage response = SignalMessage.error(reason, null);
            response = new SignalMessage(SignalType.AUTH_FAILED, null, null, response.payload());
            String jsonMessage = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(jsonMessage));

            session.close(CloseStatus.POLICY_VIOLATION.withReason(reason));
        } catch (Exception e) {
            log.error("Failed to send AUTH_FAILED", e);
        }
    }
    
    private void sendErrorToSession(WebSocketSession session, String errorMessage) {
        try {
            SignalMessage errorMsg = SignalMessage.error(errorMessage, null);
            String jsonMessage = objectMapper.writeValueAsString(errorMsg);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            log.error("Failed to send error message", e);
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
        } else {
            log.debug("Unauthenticated session disconnected. Reason: {}", status);
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

