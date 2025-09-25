package com.back.postpilot.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedInPostMetricsDTO {
    private String postId;
    private String postUrl;
    private String authorId;
    private LocalDateTime postedAt;
    private LocalDateTime lastUpdated;
    
    // Basic metrics
    private Long impressions;
    private Long clicks;
    private Long likes;
    private Long comments;
    private Long shares;
    private Long reactions;
    
    // Engagement metrics
    private Double engagementRate;
    private Double clickThroughRate;
    private Double likeRate;
    private Double commentRate;
    private Double shareRate;
    
    // Demographics (if available)
    private List<LinkedInDemographicMetrics> demographics;
    
    // Time-based metrics
    private List<LinkedInTimeSeriesMetric> timeSeriesData;
    
    // Content performance
    private String contentType;
    private String contentCategory;
    private Boolean hasMedia;
    private Integer mediaCount;
    
    // LinkedIn specific metrics
    private Long profileViews;
    private Long connectionRequests;
    private Long follows;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkedInDemographicMetrics {
        private String demographicType; // age, location, industry, etc.
        private String value;
        private Long count;
        private Double percentage;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkedInTimeSeriesMetric {
        private LocalDateTime timestamp;
        private String metricType; // impressions, clicks, likes, etc.
        private Long value;
        private String period; // hourly, daily, weekly
    }
}
