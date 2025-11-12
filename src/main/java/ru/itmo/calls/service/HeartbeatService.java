package ru.itmo.calls.service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.itmo.calls.model.SignalMessage;
import ru.itmo.calls.model.SignalType;

@Service
public class HeartbeatService {
    private static final Logger log = LoggerFactory.getLogger(HeartbeatService.class);

    private final OnlineUsersService onlineUsersService;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<Integer, Long> lastActivity = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Integer> missedPings = new ConcurrentHashMap<>();

    @Value("${signalling.heartbeat.interval:30000}")
    private long heartbeatInterval;

    @Value("${signalling.heartbeat.timeout:60000}")
    private long heartbeatTimeout;

    @Value("${signalling.heartbeat.max-missed-pings:2}")
    private int maxMissedPings;

    public HeartbeatService(OnlineUsersService onlineUsersService, ObjectMapper objectMapper) {
        this.onlineUsersService = onlineUsersService;
        this.objectMapper = objectMapper;
    }

    public void recordActivity(Integer userId) {
        lastActivity.put(userId, System.currentTimeMillis());
        missedPings.put(userId, 0);
    }

    public void handlePong(Integer userId) {
        recordActivity(userId);
        log.trace("Received pong from user {}", userId);
    }

    public void removeUser(Integer userId) {
        lastActivity.remove(userId);
        missedPings.remove(userId);
    }

    @Scheduled(fixedDelayString = "${signalling.heartbeat.interval:30000}")
    public void sendHeartbeats() {
        long currentTime = System.currentTimeMillis();

        for (Integer userId : onlineUsersService.getOnlineUsers()) {
            WebSocketSession session = onlineUsersService.getSession(userId);

            if (session == null || !session.isOpen()) {
                continue;
            }

            Long lastActivityTime = lastActivity.get(userId);
            if (lastActivityTime == null) {
                recordActivity(userId);
                continue;
            }

            long timeSinceLastActivity = currentTime - lastActivityTime;

            if (timeSinceLastActivity > heartbeatTimeout) {
                int missed = missedPings.getOrDefault(userId, 0) + 1;
                missedPings.put(userId, missed);

                if (missed > maxMissedPings) {
                    log.warn(
                            "User {} failed to respond to {} pings, closing connection",
                            userId, missed
                    );
                    try {
                        session.close(CloseStatus.GOING_AWAY.withReason("Heartbeat timeout"));
                    } catch (IOException e) {
                        log.error("Failed to close session for user {}", userId, e);
                    }
                    continue;
                }
            }

            try {
                SignalMessage ping = new SignalMessage(SignalType.PING, null, userId, null);
                String pingMessage = objectMapper.writeValueAsString(ping);
                session.sendMessage(new TextMessage(pingMessage));
                log.trace("Sent ping to user {}", userId);
            } catch (Exception e) {
                log.error("Failed to send ping to user {}", userId, e);
            }
        }
    }
}
