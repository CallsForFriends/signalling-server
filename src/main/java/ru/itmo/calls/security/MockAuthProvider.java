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

        if (token == null || token.isBlank()) {
            log.warn("Invalid or missing token");
            return null;
        }

        Integer userId = Integer.parseInt(token);
        log.debug("Mock auth: Extracted userId {} from token", userId);
        return new UserIdentity(userId);
    }
}

