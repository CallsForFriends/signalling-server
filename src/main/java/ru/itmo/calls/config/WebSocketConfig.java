package ru.itmo.calls.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import ru.itmo.calls.handler.SignallingWebSocketHandler;
import ru.itmo.calls.security.AuthHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final SignallingWebSocketHandler signallingWebSocketHandler;
    private final AuthHandshakeInterceptor authHandshakeInterceptor;
    
    @Value("${signalling.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;
    
    public WebSocketConfig(
        SignallingWebSocketHandler signallingWebSocketHandler,
        AuthHandshakeInterceptor authHandshakeInterceptor
    ) {
        this.signallingWebSocketHandler = signallingWebSocketHandler;
        this.authHandshakeInterceptor = authHandshakeInterceptor;
    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        
        registry.addHandler(signallingWebSocketHandler, "/signalling")
            .addInterceptors(authHandshakeInterceptor)
            .setAllowedOrigins(origins);
    }
}

