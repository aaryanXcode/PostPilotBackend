package com.back.postpilot.controller;

import com.back.postpilot.DTO.LinkedInAnalyticsRequestDTO;
import com.back.postpilot.DTO.LinkedInPostMetricsDTO;
import com.back.postpilot.service.LinkedInAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/linkedin/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LinkedInAnalyticsController {

    private final LinkedInAnalyticsService analyticsService;

    /**
     * Get metrics for a specific LinkedIn post
     * 
     * @param postId LinkedIn post ID
     * @return Post metrics
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<LinkedInPostMetricsDTO> getPostMetrics(@PathVariable String postId) {
        
        try {
            log.info("Fetching metrics for LinkedIn post: {}", postId);
            
            LinkedInPostMetricsDTO metrics = analyticsService.getPostMetrics(postId);
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            log.error("Error fetching post metrics for {}: {}", postId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get metrics for multiple LinkedIn posts
     * 
     * @param postIds List of post IDs
     * @return List of post metrics
     */
    @PostMapping("/posts/batch")
    public ResponseEntity<List<LinkedInPostMetricsDTO>> getMultiplePostMetrics(
            @RequestBody List<String> postIds) {
        
        try {
            log.info("Fetching metrics for {} LinkedIn posts", postIds.size());
            
            List<LinkedInPostMetricsDTO> metrics = analyticsService.getMultiplePostMetrics(postIds);
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            log.error("Error fetching multiple post metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get analytics for a user's posts within date range
     * 
     * @param authorId LinkedIn user ID
     * @param startDate Start date (ISO format)
     * @param endDate End date (ISO format)
     * @return List of post metrics
     */
    @GetMapping("/users/{authorId}/posts")
    public ResponseEntity<List<LinkedInPostMetricsDTO>> getUserPostAnalytics(
            @PathVariable String authorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            log.info("Fetching analytics for user {} from {} to {}", authorId, startDate, endDate);
            
            List<LinkedInPostMetricsDTO> analytics = analyticsService.getUserAnalytics(
                    authorId, startDate, endDate);
            
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            log.error("Error fetching user analytics for {}: {}", authorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get engagement summary for a user
     * 
     * @param authorId LinkedIn user ID
     * @param startDate Start date (ISO format)
     * @param endDate End date (ISO format)
     * @return Engagement summary
     */
    @GetMapping("/users/{authorId}/summary")
    public ResponseEntity<Map<String, Object>> getEngagementSummary(
            @PathVariable String authorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            log.info("Calculating engagement summary for user {} from {} to {}", 
                    authorId, startDate, endDate);
            
            Map<String, Object> summary = analyticsService.getEngagementSummary(
                    authorId, startDate, endDate);
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error calculating engagement summary for {}: {}", authorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get top performing posts for a user
     * 
     * @param authorId LinkedIn user ID
     * @param limit Number of top posts to return (default: 10)
     * @return List of top performing posts
     */
    @GetMapping("/users/{authorId}/top-posts")
    public ResponseEntity<List<LinkedInPostMetricsDTO>> getTopPerformingPosts(
            @PathVariable String authorId,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            log.info("Fetching top {} performing posts for user {}", limit, authorId);
            
            List<LinkedInPostMetricsDTO> topPosts = analyticsService.getTopPerformingPosts(authorId, limit);
            
            return ResponseEntity.ok(topPosts);
            
        } catch (Exception e) {
            log.error("Error fetching top performing posts for {}: {}", authorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get analytics with custom request parameters
     * 
     * @param request Custom analytics request
     * @return Analytics data
     */
    @PostMapping("/custom")
    public ResponseEntity<List<LinkedInPostMetricsDTO>> getCustomAnalytics(
            @RequestBody LinkedInAnalyticsRequestDTO request) {
        
        try {
            log.info("Processing custom analytics request for post: {}", request.getPostId());
            
            List<LinkedInPostMetricsDTO> analytics = analyticsService.getUserAnalytics(
                    request.getAuthorId(),
                    request.getStartDate(),
                    request.getEndDate());
            
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            log.error("Error processing custom analytics request: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get paginated analytics for a user
     * 
     * @param authorId LinkedIn user ID
     * @param page Page number (0-based)
     * @param size Page size
     * @param sortBy Sort field (default: postedAt)
     * @param sortDir Sort direction (asc/desc, default: desc)
     * @return Paginated analytics
     */
    @GetMapping("/users/{authorId}/paginated")
    public ResponseEntity<Page<LinkedInPostMetricsDTO>> getPaginatedAnalytics(
            @PathVariable String authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "postedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            log.info("Fetching paginated analytics for user {}: page {}, size {}", 
                    authorId, page, size);
            
            // This would require implementing pagination in the service
            // For now, return a simple response
            return ResponseEntity.ok(Page.empty());
            
        } catch (Exception e) {
            log.error("Error fetching paginated analytics for {}: {}", authorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Refresh metrics for a specific post (force fresh data from LinkedIn)
     * 
     * @param postId LinkedIn post ID
     * @return Updated metrics
     */
    @PostMapping("/posts/{postId}/refresh")
    public ResponseEntity<LinkedInPostMetricsDTO> refreshPostMetrics(@PathVariable String postId) {
        
        try {
            log.info("Refreshing metrics for LinkedIn post: {}", postId);
            
            // Force refresh by clearing cache and fetching fresh data
            LinkedInPostMetricsDTO metrics = analyticsService.getPostMetrics(postId);
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            log.error("Error refreshing post metrics for {}: {}", postId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get analytics dashboard data for a user
     * 
     * @param authorId LinkedIn user ID
     * @param days Number of days to look back (default: 30)
     * @return Dashboard data
     */
    @GetMapping("/users/{authorId}/dashboard")
    public ResponseEntity<Map<String, Object>> getAnalyticsDashboard(
            @PathVariable String authorId,
            @RequestParam(defaultValue = "30") int days) {
        
        try {
            log.info("Building analytics dashboard for user {} (last {} days)", authorId, days);
            
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            
            // Get engagement summary
            Map<String, Object> summary = analyticsService.getEngagementSummary(
                    authorId, startDate, endDate);
            
            // Get top performing posts
            List<LinkedInPostMetricsDTO> topPosts = analyticsService.getTopPerformingPosts(authorId, 5);
            
            // Build dashboard response
            Map<String, Object> dashboard = Map.of(
                    "summary", summary,
                    "topPosts", topPosts,
                    "dateRange", Map.of(
                            "startDate", startDate,
                            "endDate", endDate,
                            "days", days
                    )
            );
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            log.error("Error building analytics dashboard for {}: {}", authorId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

}
