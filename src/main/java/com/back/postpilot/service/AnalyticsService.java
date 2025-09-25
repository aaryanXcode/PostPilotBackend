package com.back.postpilot.service;

import com.back.postpilot.DTO.AnalyticsDTO;
import com.back.postpilot.DTO.PostAnalyticsDTO;
import com.back.postpilot.DTO.RecentPostDTO;
import com.back.postpilot.entity.GeneratedContent;
import com.back.postpilot.repository.AnalyticsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    public AnalyticsDTO getAnalyticsData(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting analytics data for user: {}, date range: {} to {}", userId, startDate, endDate);

        try {
            // Get total posts count (published posts only)
            Long totalPosts = (startDate != null && endDate != null) 
                ? analyticsRepository.countTotalPostsByUserAndDateRange(userId, startDate, endDate)
                : analyticsRepository.countTotalPostsByUser(userId);

            // Get generated posts count (all rows in p_generated_content for this user)
            Long generatedPostsCount = (startDate != null && endDate != null) 
                ? analyticsRepository.countAllGeneratedPostsByUserAndDateRange(userId, startDate, endDate)
                : analyticsRepository.countAllGeneratedPostsByUser(userId);

            // Get published posts count (simplified - count all published posts)
            Long publishedPostsCount = analyticsRepository.countPublishedPostsByUser();
            log.debug("Published posts count (all users): {}", publishedPostsCount);

            // Get scheduled posts count (simplified - count all scheduled posts)
            Long scheduledPostsCount = analyticsRepository.countScheduledPostsByUser();
            log.debug("Scheduled posts count (all users): {}", scheduledPostsCount);

            // Get draft posts count (simplified - count all draft posts)
            Long draftPostsCount = analyticsRepository.countDraftPostsByUser();
            log.debug("Draft posts count (all users): {}", draftPostsCount);

            log.info("Analytics data for user {}: totalPosts={}, generatedPostsCount={}, publishedPostsCount={}, scheduledPostsCount={}, draftPostsCount={}", 
                    userId, totalPosts, generatedPostsCount, publishedPostsCount, scheduledPostsCount, draftPostsCount);

            // Get recent posts for performance data
            Pageable pageable = PageRequest.of(0, 10);
            List<GeneratedContent> recentPosts = analyticsRepository.findRecentPostsByUser(userId, pageable);

            // Calculate mock engagement metrics (since we don't have real engagement data yet)
            Long totalEngagement = calculateMockEngagement(recentPosts);
            Long totalReach = calculateMockReach(recentPosts);
            Long totalImpressions = calculateMockImpressions(recentPosts);
            Double avgEngagementRate = calculateAvgEngagementRate(totalEngagement, totalReach);
            Long bestPostEngagement = calculateBestPostEngagement(recentPosts);

            // Convert recent posts to DTOs
            List<RecentPostDTO> recentPostDTOs = recentPosts.stream()
                .map(this::convertToRecentPostDTO)
                .collect(Collectors.toList());

            log.info("Analytics data for user {}: totalPosts={}, generatedPostsCount={}, publishedPostsCount={}, scheduledPostsCount={}, draftPostsCount={}", 
                    userId, totalPosts, generatedPostsCount, publishedPostsCount, scheduledPostsCount, draftPostsCount);

            return AnalyticsDTO.builder()
                .totalPosts(totalPosts)
                .generatedPostsCount(generatedPostsCount)
                .publishedPostsCount(publishedPostsCount)
                .scheduledPostsCount(scheduledPostsCount)
                .draftPostsCount(draftPostsCount)
                .totalEngagement(totalEngagement)
                .totalReach(totalReach)
                .totalImpressions(totalImpressions)
                .avgEngagementRate(avgEngagementRate)
                .bestPostEngagement(bestPostEngagement)
                .totalFollowers(1200L) // Mock data - replace with real LinkedIn followers
                .recentPosts(recentPostDTOs)
                .build();

        } catch (Exception e) {
            log.error("Error getting analytics data for user: {}", userId, e);
            throw new RuntimeException("Failed to retrieve analytics data", e);
        }
    }

    public PostAnalyticsDTO getPostAnalytics(Long userId, Long postId) {
        log.debug("Getting analytics for post: {} by user: {}", postId, userId);

        try {
            // Find the specific post
            GeneratedContent post = analyticsRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

            // Verify the post belongs to the user
            if (!post.getChatSession().getUserId().equals(userId)) {
                throw new RuntimeException("Access denied: Post does not belong to user");
            }

            // Calculate mock engagement data
            Long engagement = calculateMockEngagement(List.of(post));
            Long reach = calculateMockReach(List.of(post));
            Long impressions = calculateMockImpressions(List.of(post));
            Double engagementRate = calculateAvgEngagementRate(engagement, reach);

            return PostAnalyticsDTO.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .engagement(engagement)
                .reach(reach)
                .impressions(impressions)
                .engagementRate(engagementRate)
                .status(post.getStatus().toString())
                .platform(post.getPlatform().toString())
                .createdAt(post.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .publishedAt(post.getStatus().toString().equals("PUBLISHED") ? 
                    post.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .build();

        } catch (Exception e) {
            log.error("Error getting post analytics for post: {} by user: {}", postId, userId, e);
            throw new RuntimeException("Failed to retrieve post analytics", e);
        }
    }



    // Helper methods for mock calculations (replace with real data when available)
    private Long calculateMockEngagement(List<GeneratedContent> posts) {
        // Mock calculation based on post age and content length
        return posts.stream()
            .mapToLong(post -> {
                long baseEngagement = 10 + (post.getContent() != null ? post.getContent().length() / 10 : 0);
                long ageFactor = Math.max(1, 30 - java.time.temporal.ChronoUnit.DAYS.between(post.getCreatedAt(), LocalDateTime.now()));
                return baseEngagement * ageFactor;
            })
            .sum();
    }

    private Long calculateMockReach(List<GeneratedContent> posts) {
        // Mock calculation: reach is typically 3-5x engagement
        return (long) (calculateMockEngagement(posts) * 4.2);
    }

    private Long calculateMockImpressions(List<GeneratedContent> posts) {
        // Mock calculation: impressions are typically 10-15x engagement
        return (long) (calculateMockEngagement(posts) * 12.5);
    }

    private Double calculateAvgEngagementRate(Long engagement, Long reach) {
        if (reach == 0) return 0.0;
        return (engagement.doubleValue() / reach.doubleValue()) * 100;
    }

    private Long calculateBestPostEngagement(List<GeneratedContent> posts) {
        if (posts.isEmpty()) return 0L;
        return posts.stream()
            .mapToLong(post -> calculateMockEngagement(List.of(post)))
            .max()
            .orElse(0L);
    }

    private RecentPostDTO convertToRecentPostDTO(GeneratedContent post) {
        Long engagement = calculateMockEngagement(List.of(post));
        Long reach = calculateMockReach(List.of(post));
        Long impressions = calculateMockImpressions(List.of(post));

        return RecentPostDTO.builder()
            .id(post.getId())
            .title(post.getTitle() != null ? post.getTitle() : "Untitled Post")
            .engagement(engagement)
            .reach(reach)
            .impressions(impressions)
            .status(post.getStatus().toString())
            .createdAt(post.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .build();
    }
}
