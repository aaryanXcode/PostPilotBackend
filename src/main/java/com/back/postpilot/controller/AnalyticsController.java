package com.back.postpilot.controller;

import com.back.postpilot.DTO.AnalyticsDTO;
import com.back.postpilot.DTO.PostAnalyticsDTO;
import com.back.postpilot.entity.UserEntity;
import com.back.postpilot.service.AnalyticsService;
import com.back.postpilot.service.CustomUserDetailsService;
import com.back.postpilot.service.JWTService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private com.back.postpilot.repository.AnalyticsRepository analyticsRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('SUPER_ADMIN') OR hasRole('USER')")
    public ResponseEntity<?> getAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest request) {
        
        try {
            log.debug("Getting analytics data with date range: {} to {}", startDate, endDate);
            
            // Extract user ID from JWT token
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or missing authentication token"));
            }

            // Debug: Test the repository query directly
            Long directCount = analyticsRepository.countAllGeneratedPostsByUser(userId);
            log.info("Direct repository query result for user {}: {}", userId, directCount);

            // Get analytics data
            AnalyticsDTO analyticsData = analyticsService.getAnalyticsData(userId, startDate, endDate);
            
            log.info("Successfully retrieved analytics data for user: {}, generatedPostsCount: {}", 
                    userId, analyticsData.getGeneratedPostsCount());
            return ResponseEntity.ok(analyticsData);

        } catch (Exception e) {
            log.error("Error retrieving analytics data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve analytics data", "message", e.getMessage()));
        }
    }

    @GetMapping("/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('SUPER_ADMIN') OR hasRole('USER')")
    public ResponseEntity<?> getPostAnalytics(
            @PathVariable Long postId,
            HttpServletRequest request) {
        
        try {
            log.debug("Getting analytics for post: {}", postId);
            
            // Extract user ID from JWT token
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or missing authentication token"));
            }

            // Get post analytics
            PostAnalyticsDTO postAnalytics = analyticsService.getPostAnalytics(userId, postId);
            
            log.debug("Successfully retrieved analytics for post: {} by user: {}", postId, userId);
            return ResponseEntity.ok(postAnalytics);

        } catch (RuntimeException e) {
            log.error("Error retrieving post analytics for post: {}", postId, e);
            if (e.getMessage().contains("not found") || e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve post analytics", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error retrieving post analytics for post: {}", postId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve post analytics", "message", e.getMessage()));
        }
    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('SUPER_ADMIN') OR hasRole('USER')")
    public ResponseEntity<?> testDatabase(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or missing authentication token"));
            }

            // Test direct repository queries
            log.info("🔍 Testing database queries for user ID: {}", userId);
            
            Long totalPosts = analyticsRepository.countTotalPostsByUser(userId);
            log.info("📊 countTotalPostsByUser({}) = {}", userId, totalPosts);
            
            Long generatedPosts = analyticsRepository.countAllGeneratedPostsByUser(userId);
            log.info("📊 countAllGeneratedPostsByUser({}) = {}", userId, generatedPosts);
            
            Long publishedPosts = analyticsRepository.countPublishedPostsByUser();
            log.info("📊 countPublishedPostsByUser() = {}", publishedPosts);
            
            Long scheduledPosts = analyticsRepository.countScheduledPostsByUser();
            log.info("📊 countScheduledPostsByUser() = {}", scheduledPosts);
            
            Long draftPosts = analyticsRepository.countDraftPostsByUser();
            log.info("📊 countDraftPostsByUser() = {}", draftPosts);

            // Test if there are any chat sessions for this user
            List<com.back.postpilot.entity.ChatSession> chatSessions = analyticsRepository.findAllChatSessionsByUser(userId);
            log.info("📊 Chat sessions for user {}: {}", userId, chatSessions.size());

            // Test if there are any generated content records at all
            Long totalGeneratedContent = analyticsRepository.countAllGeneratedContent();
            log.info("📊 Total generated content in database: {}", totalGeneratedContent);

            // Get all generated content to see what's in the database
            List<com.back.postpilot.entity.GeneratedContent> allGeneratedContent = analyticsRepository.findAllGeneratedContent();
            log.info("📊 All generated content records: {}", allGeneratedContent.size());
            for (com.back.postpilot.entity.GeneratedContent gc : allGeneratedContent) {
                log.info("📊 GeneratedContent ID: {}, ChatSession ID: {}, User ID: {}", 
                        gc.getId(), 
                        gc.getChatSession() != null ? gc.getChatSession().getId() : "NULL",
                        gc.getChatSession() != null ? gc.getChatSession().getUserId() : "NULL");
            }

            // Test with user ID 2 as well (in case the data belongs to user 2)
            Long generatedPostsForUser2 = analyticsRepository.countAllGeneratedPostsByUser(2L);
            log.info("📊 countAllGeneratedPostsByUser(2) = {}", generatedPostsForUser2);

            // Test with user ID 2 for all metrics (using the same simplified queries)
            Long publishedPostsForUser2 = analyticsRepository.countPublishedPostsByUser();
            Long scheduledPostsForUser2 = analyticsRepository.countScheduledPostsByUser();
            Long draftPostsForUser2 = analyticsRepository.countDraftPostsByUser();
            log.info("📊 All users metrics - Published: {}, Scheduled: {}, Draft: {}", 
                    publishedPostsForUser2, scheduledPostsForUser2, draftPostsForUser2);

            // Debug: Check what status values exist in the database
            List<String> distinctStatuses = analyticsRepository.findAllDistinctStatuses();
            log.info("📊 Distinct statuses in database: {}", distinctStatuses);

            // Debug: Check what scheduledAt values exist in the database
            List<java.time.LocalDateTime> distinctScheduledAt = analyticsRepository.findAllDistinctScheduledAt();
            log.info("📊 Distinct scheduledAt values in database: {}", distinctScheduledAt);

            // Debug: Check which user IDs have generated content
            List<Long> distinctUserIds = analyticsRepository.findAllDistinctUserIds();
            log.info("📊 Distinct user IDs with generated content: {}", distinctUserIds);

            // Debug: Get all generated content with user info
            List<Object[]> allContentWithUserInfo = analyticsRepository.findAllGeneratedContentWithUserInfo();
            log.info("📊 All generated content with user info:");
            for (Object[] row : allContentWithUserInfo) {
                log.info("📊 Content ID: {}, Status: {}, ScheduledAt: {}, UserId: {}", row[0], row[1], row[2], row[3]);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("totalPosts", totalPosts);
            response.put("generatedPosts", generatedPosts);
            response.put("publishedPosts", publishedPosts);
            response.put("scheduledPosts", scheduledPosts);
            response.put("draftPosts", draftPosts);
            response.put("chatSessionsCount", chatSessions.size());
            response.put("totalGeneratedContentInDB", totalGeneratedContent);
            response.put("allGeneratedContentCount", allGeneratedContent.size());
            response.put("generatedPostsForUser2", generatedPostsForUser2);
            response.put("publishedPostsForUser2", publishedPostsForUser2);
            response.put("scheduledPostsForUser2", scheduledPostsForUser2);
            response.put("draftPostsForUser2", draftPostsForUser2);
            response.put("distinctStatuses", distinctStatuses);
            response.put("distinctScheduledAt", distinctScheduledAt);
            response.put("distinctUserIds", distinctUserIds);
            response.put("allContentWithUserInfo", allContentWithUserInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in test endpoint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }



    /**
     * Extract user ID from JWT token in the request
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }

            String token = authHeader.substring(7);
            
            // Validate token first
            if (!jwtService.validateToken(token)) {
                log.error("Invalid JWT token");
                return null;
            }
            
            // Extract username from token
            String username = jwtService.extractUserName(token);
            
            // Get user entity from database to get the user ID
            UserEntity user = userDetailsService.findByUsername(username);
            log.info("Extracted user ID: {} for username: {}", user.getId(), username);
            return user.getId();
            
        } catch (Exception e) {
            log.error("Error extracting user ID from token", e);
            return null;
        }
    }
}
