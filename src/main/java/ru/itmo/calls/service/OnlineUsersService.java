package ru.itmo.calls.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUsersService {
    private static final Logger log = LoggerFactory.getLogger(OnlineUsersService.class);
    
    private final ConcurrentHashMap<Integer, WebSocketSession> onlineUsers = new ConcurrentHashMap<>();

    public void registerUser(Integer userId, WebSocketSession session) {
        WebSocketSession existingSession = onlineUsers.put(userId, session);

        if (existingSession != null && existingSession.isOpen()) {
            log.warn("User {} already had an active session, closing the old one", userId);
            try {
                existingSession.close(CloseStatus.POLICY_VIOLATION.withReason("Duplicate session"));
            } catch (Exception e) {
                log.error("Failed to close existing session for user {}", userId, e);
            }
        }
        
        log.info("User {} registered as online. Total online: {}", userId, onlineUsers.size());
    }

    public void unregisterUser(Integer userId) {
        try(WebSocketSession removed = onlineUsers.remove(userId)){
            if (removed != null) {
                log.info("User {} unregistered. Total online: {}", userId, onlineUsers.size());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public WebSocketSession getSession(Integer userId) {
        return onlineUsers.get(userId);
    }

    public boolean isUserOffline(Integer userId) {
        return !onlineUsers.containsKey(userId);
    }

    public List<Integer> getOnlineUsers() {
        return List.copyOf(onlineUsers.keySet());
    }

    public int getOnlineCount() {
        return onlineUsers.size();
    }
}

