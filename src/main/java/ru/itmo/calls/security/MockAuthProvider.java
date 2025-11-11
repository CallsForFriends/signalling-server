package ru.itmo.calls.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import ru.itmo.calls.model.UserIdentity;

@Component
@ConditionalOnProperty(name = "auth.provider.enabled", havingValue = "false")
public class MockAuthProvider implements AuthProvider {

    private static final Logger log = LoggerFactory.getLogger(MockAuthProvider.class);

    @Override
    public UserIdentity validateAndExtractIdentity(ServerHttpRequest request) {
        String token = extractToken(request);
        return validateToken(token);
    }
    
    @Override
    public UserIdentity validateToken(String token) {
        if (token == null || token.isBlank()) {
            log.warn("Invalid or missing token");
            return null;
        }

        try {
            Integer userId = Integer.parseInt(token);
            log.debug("Mock auth: Extracted userId {} from token", userId);
            return new UserIdentity(userId);
        } catch (NumberFormatException e) {
            // If token is not a number, use hash code as userId for testing
            Integer userId = Math.abs(token.hashCode() % 10000);
            log.debug("Mock auth: Generated userId {} from token hash", userId);
            return new UserIdentity(userId);
        }
    }
}

