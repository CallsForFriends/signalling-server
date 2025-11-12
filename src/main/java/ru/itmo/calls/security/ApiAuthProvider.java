package ru.itmo.calls.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.itmo.calls.model.UserIdentity;
import ru.itmo.calls.model.dto.UserMeResponse;

@Component
@ConditionalOnProperty(name = "auth.provider.enabled", havingValue = "true", matchIfMissing = true)
public class ApiAuthProvider implements AuthProvider {
    private static final Logger log = LoggerFactory.getLogger(ApiAuthProvider.class);
    private static final String USER_ENDPOINT = "/api/v1/users/me";

    private final RestTemplate restTemplate;

    public ApiAuthProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<UserMeResponse> response = restTemplate.exchange(
                    USER_ENDPOINT,
                    HttpMethod.GET,
                    entity,
                    UserMeResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UserMeResponse userResponse = response.getBody();
                if (userResponse.user() != null && userResponse.user().id() != null) {
                    Integer userId = userResponse.user().id();
                    log.debug("Successfully validated token for user: {}", userId);
                    return new UserIdentity(userId);
                }
            }

            log.warn("Invalid response from auth API");
            return null;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.debug("Token validation failed: unauthorized");
            } else {
                log.warn("Token validation failed with status: {}", e.getStatusCode());
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to validate token", e);
            return null;
        }
    }
}

