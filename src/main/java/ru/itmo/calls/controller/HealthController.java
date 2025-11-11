package ru.itmo.calls.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.calls.service.OnlineUsersService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {
    
    private final OnlineUsersService onlineUsersService;
    
    public HealthController(OnlineUsersService onlineUsersService) {
        this.onlineUsersService = onlineUsersService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "signalling-server",
            "onlineUsers", onlineUsersService.getOnlineCount()
        ));
    }

    @GetMapping("/users/online")
    public ResponseEntity<Map<String, Object>> getOnlineUsers() {
        List<Integer> onlineUsers = onlineUsersService.getOnlineUsers();
        
        return ResponseEntity.ok(Map.of(
            "count", onlineUsers.size(),
            "users", onlineUsers
        ));
    }
}

