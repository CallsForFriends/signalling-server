package ru.itmo.calls.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import ru.itmo.calls.model.UserIdentity;

@Component
@ConditionalOnProperty(name = "auth.provider.enabled", havingValue = "true", matchIfMissing = true)
public class ApiAuthProvider implements AuthProvider {
    private static final Logger log = LoggerFactory.getLogger(ApiAuthProvider.class);
    
    @Value("${auth.provider.api.url}")
    private String apiUrl;
    
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
        
        // TODO: Implement actual REST API call to validate token
        log.error("ApiAuthProvider is not implemented yet");
        return null;
    }
}

