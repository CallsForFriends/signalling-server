package ru.itmo.calls.config;

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

    public WebSocketConfig(
            SignallingWebSocketHandler signallingWebSocketHandler,
            AuthHandshakeInterceptor authHandshakeInterceptor
    ) {
        this.signallingWebSocketHandler = signallingWebSocketHandler;
        this.authHandshakeInterceptor = authHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(signallingWebSocketHandler, "/signalling")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}

