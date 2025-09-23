package com.back.postpilot.notification;

import com.back.postpilot.DTO.UserNotifyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PushNotification implements INotification{
    private static final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    @Override
    public void send(UserNotifyDTO user, NotificationEvent event){
        String userId = user.getUserId().toString();
        SseEmitter emitter = userEmitters.get(userId);
        if(emitter != null){
            try{
                String jsonData = String.format(
                        "{\"title\":\"📱 Scheduled Post Published!\",\"message\":\"Your post is now live!\",\"postId\":\"%s\",\"userId\":\"%s\",\"timestamp\":\"%s\"}",
                        event.getPostId(), user.getUserId(), LocalDateTime.now()
                );
                emitter.send(SseEmitter.event().name("notification").data(jsonData));
                log.debug("\uD83D\uDCE1 Sending SSE notification to User ID: {} for Post ID: {}", user.getUserId(), event.getPostId());

            } catch (Exception e) {
                log.error("❌ Failed to send SSE notification to user: {} - {}", userId, e.getMessage());
                userEmitters.remove(userId); // Remove broken connection
            }
        }
        else {
            log.debug(" No active SSE connection for User ID: {} (user not online)", user.getUserId());
        }

    }

    public static SseEmitter createConnection(String userId){
        SseEmitter emitter  = new SseEmitter(0L);
        userEmitters.put(userId,emitter);

        emitter.onCompletion(()->{
            userEmitters.remove(userId);
            log.debug("SSE connection completed for user: {}", userId);
        });

        emitter.onTimeout(()->{
            userEmitters.remove(userId);
            log.debug("⏰ SSE connection timeout for user: {}", userId);
        });

        emitter.onError((ex)->{
            userEmitters.remove(userId);
            log.debug("\uD83D\uDCA5 SSE connection error for user: {} - {}", userId, ex.getMessage());
        });
        log.debug("✅ SSE connection established for user: {} (Total connections: {})", userId, userEmitters.size());
        return emitter;
    }

    public static void removeConnection(String userId){
        SseEmitter emitter = userEmitters.remove(userId);
        if(emitter != null){
            try{
                emitter.complete();
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
            log.debug("SSE connection removed for user: {}", userId);
        }
    }
    public static boolean isUserConnected(String userId) {
        return userEmitters.containsKey(userId);
    }

    public static int getActiveConnectionsCount() {
        return userEmitters.size();
    }

    public static void broadcastToAllConnections(String message) {
        userEmitters.entrySet().removeIf(entry -> {
            try {
                entry.getValue().send(SseEmitter.event()
                        .name("broadcast")
                        .data(message));
                return false; // Keep connection
            } catch (IOException e) {
                log.error("Failed to broadcast to user: {}", entry.getKey());
                return true;
            }
        });
    }
}
