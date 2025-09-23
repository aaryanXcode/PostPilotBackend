package com.back.postpilot.controller;

import com.back.postpilot.DTO.UserNotifyDTO;
import com.back.postpilot.EnumTypeConstants.NotificationType;
import com.back.postpilot.notification.NotificationEvent;
import com.back.postpilot.notification.PushNotification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
public class SSEController {

    @GetMapping("/api/notifications/test")
    @CrossOrigin(origins = "http://localhost:5173")
    public String testNotification(@RequestParam String userId) {
        UserNotifyDTO user =  new UserNotifyDTO(
                Long.parseLong(userId),
                "Aryan",
                "aryan@example.com",
                List.of(NotificationType.PUSH)
        );

        NotificationEvent event = new NotificationEvent(2L); // your DTO
        new PushNotification().send(user, event);
        return "Test notification sent!";
    }

    @GetMapping(value = "/api/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @CrossOrigin(origins = "http://localhost:5173")
    public SseEmitter streamNotifications(@RequestParam String userId) {

        // Create SSE connection using refactored PushNotification class
        SseEmitter emitter = PushNotification.createConnection(userId);

        // Send connection confirmation
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"status\":\"connected\",\"message\":\"Connected to PostPilot notifications\",\"userId\":\"" + userId + "\"}"));
        } catch (IOException e) {
            System.err.println("Failed to send initial message to user: " + userId);
            PushNotification.removeConnection(userId);
        }

        return emitter;
    }

    // Optional: Endpoint to check connection status
    @GetMapping("/api/notifications/status")
    public String getConnectionStatus(@RequestParam String userId) {
        boolean connected = PushNotification.isUserConnected(userId);
        int totalConnections = PushNotification.getActiveConnectionsCount();

        return String.format(
                "{\"userId\":\"%s\",\"connected\":%b,\"totalConnections\":%d}",
                userId, connected, totalConnections
        );
    }
}
