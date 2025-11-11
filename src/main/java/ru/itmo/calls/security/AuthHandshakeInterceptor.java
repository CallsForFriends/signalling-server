package ru.itmo.calls.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import ru.itmo.calls.model.UserIdentity;

import java.util.Map;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AuthHandshakeInterceptor.class);
    private static final String USER_IDENTITY_ATTRIBUTE = "userIdentity";
    
    private final AuthProvider authProvider;
    
    public AuthHandshakeInterceptor(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }
    
    @Override
    public boolean beforeHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes
    ) {
        UserIdentity userIdentity = authProvider.validateAndExtractIdentity(request);
        
        if (userIdentity != null && userIdentity.userId() != null) {
            attributes.put(USER_IDENTITY_ATTRIBUTE, userIdentity);
            log.info("WebSocket handshake successful for user {} (header auth)", userIdentity.userId());
            return true;
        }

        log.debug("WebSocket connection allowed without header auth, expecting AUTH message");
        return true;
    }
    
    @Override
    public void afterHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Exception exception
    ) {
        if (exception != null) {
            log.error("Error during WebSocket handshake", exception);
        }
    }

    public static UserIdentity getUserIdentity(Map<String, Object> attributes) {
        return (UserIdentity) attributes.get(USER_IDENTITY_ATTRIBUTE);
    }
    
    public static void setUserIdentity(Map<String, Object> attributes, UserIdentity userIdentity) {
        attributes.put(USER_IDENTITY_ATTRIBUTE, userIdentity);
    }
    
    public static boolean isAuthenticated(Map<String, Object> attributes) {
        UserIdentity identity = getUserIdentity(attributes);
        return identity != null && identity.userId() != null;
    }
}

