package ru.itmo.calls.security;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import ru.itmo.calls.model.UserIdentity;

public interface AuthProvider {
    UserIdentity validateAndExtractIdentity(ServerHttpRequest request);
    UserIdentity validateToken(String token);

    default String extractToken(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().get("Authorization"))
                .map(List::getFirst)
                .filter(StringUtils::isNotBlank)
                .filter(token -> token.startsWith("Bearer "))
                .map(token -> token.substring(7))
                .orElse(null);
    }
}

