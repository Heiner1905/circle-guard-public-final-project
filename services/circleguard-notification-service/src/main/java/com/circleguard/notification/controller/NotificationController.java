package com.circleguard.notification.controller;

import com.circleguard.notification.service.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationDispatcher dispatcher;

    @PostMapping("/dispatch")
    public ResponseEntity<Map<String, String>> dispatch(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String status = request.get("status");

        if (userId == null || userId.isBlank() || status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "userId and status are required"));
        }

        dispatcher.dispatch(userId, status);
        return ResponseEntity.accepted().body(Map.of("message", "Notification dispatch accepted"));
    }
}
