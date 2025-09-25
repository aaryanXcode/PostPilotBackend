package com.back.postpilot.service;

import com.back.postpilot.DTO.LinkedInAnalyticsRequestDTO;
import com.back.postpilot.DTO.LinkedInPostMetricsDTO;
import com.back.postpilot.entity.LinkedInPostMetrics;
import com.back.postpilot.repository.LinkedInPostMetricsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.back.postpilot.controller.LinkedInOAuthController.token;
import static com.back.postpilot.linkedInAuth.Constants.USER_AGENT_OAUTH_VALUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkedInAnalyticsService {

    private final LinkedInPostMetricsRepository metricsRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${linkedin.api.base-url:https://api.linkedin.com/v2}")
    private String linkedInApiBaseUrl;

    // LinkedIn Analytics API endpoints
    private static final String ORGANIZATIONAL_SHARE_STATS = "/organizationalEntityShareStatistics";
    private static final String SHARE_STATS = "/shareStatistics";
    private static final String SOCIAL_ACTIONS = "/socialActions";
    private static final String SHARES = "/shares";

    /**
     * Fetch metrics for a specific LinkedIn post
     */
    public LinkedInPostMetricsDTO getPostMetrics(String postId) {
        try {
            log.info("Fetching metrics for post: {}", postId);
            
            // Check if we have cached metrics
            Optional<LinkedInPostMetrics> cachedMetrics = metricsRepository.findByPostId(postId);
            if (cachedMetrics.isPresent() && isRecent(cachedMetrics.get().getLastUpdated())) {
                log.info("Returning cached metrics for post: {}", postId);
                return convertToDTO(cachedMetrics.get());
            }
            
            // Fetch fresh metrics from LinkedIn API using static token
            LinkedInPostMetricsDTO freshMetrics = fetchMetricsFromLinkedIn(postId, token);
            
            // Save to database
            saveMetricsToDatabase(freshMetrics);
            
            return freshMetrics;
            
        } catch (Exception e) {
            log.error("Error fetching metrics for post {}: {}", postId, e.getMessage());
            throw new RuntimeException("Failed to fetch LinkedIn metrics", e);
        }
    }

    /**
     * Fetch metrics for multiple posts
     */
    public List<LinkedInPostMetricsDTO> getMultiplePostMetrics(List<String> postIds) {
        List<LinkedInPostMetricsDTO> results = new ArrayList<>();
        
        for (String postId : postIds) {
            try {
                LinkedInPostMetricsDTO metrics = getPostMetrics(postId);
                results.add(metrics);
            } catch (Exception e) {
                log.warn("Failed to fetch metrics for post {}: {}", postId, e.getMessage());
            }
        }
        
        return results;
    }

    /**
     * Get analytics for a user's posts with date range
     */
    public List<LinkedInPostMetricsDTO> getUserAnalytics(String authorId, LocalDateTime startDate, 
                                                         LocalDateTime endDate) {
        try {
            log.info("Fetching analytics for user: {} from {} to {}", authorId, startDate, endDate);
            
            // Get cached metrics first
            List<LinkedInPostMetrics> cachedMetrics = metricsRepository
                    .findByAuthorIdAndPostedAtBetweenOrderByPostedAtDesc(authorId, startDate, endDate);
            
            // Convert to DTOs
            List<LinkedInPostMetricsDTO> results = cachedMetrics.stream()
                    .map(this::convertToDTO)
                    .toList();
            
            // If we need fresh data, fetch from LinkedIn API
            if (results.isEmpty() || needsRefresh(cachedMetrics)) {
                log.info("Fetching fresh analytics from LinkedIn API");
                results = fetchUserAnalyticsFromLinkedIn(authorId, startDate, endDate, token);
            }
            
            return results;
            
        } catch (Exception e) {
            log.error("Error fetching user analytics for {}: {}", authorId, e.getMessage());
            throw new RuntimeException("Failed to fetch user analytics", e);
        }
    }

    /**
     * Get engagement summary for a user
     */
    public Map<String, Object> getEngagementSummary(String authorId, LocalDateTime startDate, 
                                                    LocalDateTime endDate) {
        try {
            log.info("Calculating engagement summary for user: {}", authorId);
            
            Object[] summary = metricsRepository.getMetricsSummaryForDateRange(authorId, startDate, endDate);
            
            Map<String, Object> result = new HashMap<>();
            if (summary != null && summary.length >= 7) {
                result.put("totalPosts", summary[0]);
                result.put("averageEngagementRate", summary[1]);
                result.put("totalImpressions", summary[2]);
                result.put("totalClicks", summary[3]);
                result.put("totalLikes", summary[4]);
                result.put("totalComments", summary[5]);
                result.put("totalShares", summary[6]);
                
                // Calculate additional metrics
                Long totalImpressions = (Long) summary[2];
                Long totalClicks = (Long) summary[3];
                Long totalEngagement = (Long) summary[4] + (Long) summary[5] + (Long) summary[6];
                
                if (totalImpressions != null && totalImpressions > 0) {
                    result.put("overallClickThroughRate", (double) totalClicks / totalImpressions);
                    result.put("overallEngagementRate", (double) totalEngagement / totalImpressions);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Error calculating engagement summary: {}", e.getMessage());
            throw new RuntimeException("Failed to calculate engagement summary", e);
        }
    }

    /**
     * Get top performing posts
     */
    public List<LinkedInPostMetricsDTO> getTopPerformingPosts(String authorId, int limit) {
        try {
            log.info("Fetching top performing posts for user: {}", authorId);
            
            List<LinkedInPostMetrics> topPosts = metricsRepository
                    .findTopPerformingPostsByEngagement(authorId, 
                            org.springframework.data.domain.PageRequest.of(0, limit));
            
            return topPosts.stream()
                    .map(this::convertToDTO)
                    .toList();
            
        } catch (Exception e) {
            log.error("Error fetching top performing posts: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch top performing posts", e);
        }
    }

    // Private helper methods

    private LinkedInPostMetricsDTO fetchMetricsFromLinkedIn(String postId, String accessToken) {
        try {
            // LinkedIn Analytics API call
            String url = linkedInApiBaseUrl + ORGANIZATIONAL_SHARE_STATS + "?q=organizationalEntity&organizationalEntity=" + postId;
            
            HttpHeaders headers = createHeaders(accessToken);
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return parseLinkedInResponse(response.getBody(), postId);
            } else {
                log.warn("LinkedIn API returned non-2xx status: {}", response.getStatusCode());
                return createEmptyMetrics(postId);
            }
            
        } catch (Exception e) {
            log.error("Error calling LinkedIn API for post {}: {}", postId, e.getMessage());
            return createEmptyMetrics(postId);
        }
    }

    private List<LinkedInPostMetricsDTO> fetchUserAnalyticsFromLinkedIn(String authorId, 
                                                                       LocalDateTime startDate, 
                                                                       LocalDateTime endDate, 
                                                                       String accessToken) {
        // This would require LinkedIn's organizational analytics API
        // For now, return cached data
        log.info("LinkedIn user analytics API not fully implemented, returning cached data");
        return new ArrayList<>();
    }

    private LinkedInPostMetricsDTO parseLinkedInResponse(String responseBody, String postId) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            return LinkedInPostMetricsDTO.builder()
                    .postId(postId)
                    .impressions(extractLongValue(root, "impressionCount"))
                    .clicks(extractLongValue(root, "clickCount"))
                    .likes(extractLongValue(root, "likeCount"))
                    .comments(extractLongValue(root, "commentCount"))
                    .shares(extractLongValue(root, "shareCount"))
                    .reactions(extractLongValue(root, "reactionCount"))
                    .engagementRate(calculateEngagementRate(root))
                    .clickThroughRate(calculateClickThroughRate(root))
                    .postedAt(LocalDateTime.now()) // LinkedIn doesn't always provide this
                    .lastUpdated(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error parsing LinkedIn response: {}", e.getMessage());
            return createEmptyMetrics(postId);
        }
    }

    private Long extractLongValue(JsonNode root, String fieldName) {
        JsonNode node = root.path("elements").path(0).path("totalShareStatistics").path(fieldName);
        return node.isMissingNode() ? 0L : node.asLong();
    }

    private Double calculateEngagementRate(JsonNode root) {
        long impressions = extractLongValue(root, "impressionCount");
        long likes = extractLongValue(root, "likeCount");
        long comments = extractLongValue(root, "commentCount");
        long shares = extractLongValue(root, "shareCount");
        
        if (impressions > 0) {
            return (double) (likes + comments + shares) / impressions;
        }
        return 0.0;
    }

    private Double calculateClickThroughRate(JsonNode root) {
        long impressions = extractLongValue(root, "impressionCount");
        long clicks = extractLongValue(root, "clickCount");
        
        if (impressions > 0) {
            return (double) clicks / impressions;
        }
        return 0.0;
    }

    private LinkedInPostMetricsDTO createEmptyMetrics(String postId) {
        return LinkedInPostMetricsDTO.builder()
                .postId(postId)
                .impressions(0L)
                .clicks(0L)
                .likes(0L)
                .comments(0L)
                .shares(0L)
                .reactions(0L)
                .engagementRate(0.0)
                .clickThroughRate(0.0)
                .postedAt(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private void saveMetricsToDatabase(LinkedInPostMetricsDTO metricsDTO) {
        try {
            LinkedInPostMetrics entity = convertToEntity(metricsDTO);
            metricsRepository.save(entity);
            log.info("Saved metrics to database for post: {}", metricsDTO.getPostId());
        } catch (Exception e) {
            log.error("Error saving metrics to database: {}", e.getMessage());
        }
    }

    private LinkedInPostMetrics convertToEntity(LinkedInPostMetricsDTO dto) {
        return LinkedInPostMetrics.builder()
                .postId(dto.getPostId())
                .postUrl(dto.getPostUrl())
                .authorId(dto.getAuthorId())
                .postedAt(dto.getPostedAt())
                .lastUpdated(dto.getLastUpdated())
                .impressions(dto.getImpressions())
                .clicks(dto.getClicks())
                .likes(dto.getLikes())
                .comments(dto.getComments())
                .shares(dto.getShares())
                .reactions(dto.getReactions())
                .engagementRate(dto.getEngagementRate())
                .clickThroughRate(dto.getClickThroughRate())
                .likeRate(dto.getLikeRate())
                .commentRate(dto.getCommentRate())
                .shareRate(dto.getShareRate())
                .profileViews(dto.getProfileViews())
                .connectionRequests(dto.getConnectionRequests())
                .follows(dto.getFollows())
                .contentType(dto.getContentType())
                .contentCategory(dto.getContentCategory())
                .hasMedia(dto.getHasMedia())
                .mediaCount(dto.getMediaCount())
                .build();
    }

    private LinkedInPostMetricsDTO convertToDTO(LinkedInPostMetrics entity) {
        return LinkedInPostMetricsDTO.builder()
                .postId(entity.getPostId())
                .postUrl(entity.getPostUrl())
                .authorId(entity.getAuthorId())
                .postedAt(entity.getPostedAt())
                .lastUpdated(entity.getLastUpdated())
                .impressions(entity.getImpressions())
                .clicks(entity.getClicks())
                .likes(entity.getLikes())
                .comments(entity.getComments())
                .shares(entity.getShares())
                .reactions(entity.getReactions())
                .engagementRate(entity.getEngagementRate())
                .clickThroughRate(entity.getClickThroughRate())
                .likeRate(entity.getLikeRate())
                .commentRate(entity.getCommentRate())
                .shareRate(entity.getShareRate())
                .profileViews(entity.getProfileViews())
                .connectionRequests(entity.getConnectionRequests())
                .follows(entity.getFollows())
                .contentType(entity.getContentType())
                .contentCategory(entity.getContentCategory())
                .hasMedia(entity.getHasMedia())
                .mediaCount(entity.getMediaCount())
                .build();
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT_OAUTH_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.set(HttpHeaders.ACCEPT, "application/json");
        return headers;
    }

    private boolean isRecent(LocalDateTime lastUpdated) {
        return lastUpdated != null && lastUpdated.isAfter(LocalDateTime.now().minusHours(1));
    }

    private boolean needsRefresh(List<LinkedInPostMetrics> cachedMetrics) {
        return cachedMetrics.isEmpty() || 
               cachedMetrics.stream().anyMatch(m -> m.getLastUpdated().isBefore(LocalDateTime.now().minusHours(6)));
    }
}
