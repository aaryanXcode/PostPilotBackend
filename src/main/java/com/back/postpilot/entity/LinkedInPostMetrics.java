package com.back.postpilot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "p_linkedin_post_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedInPostMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "post_id", unique = true, nullable = false)
    private String postId;
    
    @Column(name = "post_url")
    private String postUrl;
    
    @Column(name = "author_id", nullable = false)
    private String authorId;
    
    @Column(name = "posted_at")
    private LocalDateTime postedAt;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    // Basic metrics
    @Column(name = "impressions")
    private Long impressions;
    
    @Column(name = "clicks")
    private Long clicks;
    
    @Column(name = "likes")
    private Long likes;
    
    @Column(name = "comments")
    private Long comments;
    
    @Column(name = "shares")
    private Long shares;
    
    @Column(name = "reactions")
    private Long reactions;
    
    // Engagement rates
    @Column(name = "engagement_rate")
    private Double engagementRate;
    
    @Column(name = "click_through_rate")
    private Double clickThroughRate;
    
    @Column(name = "like_rate")
    private Double likeRate;
    
    @Column(name = "comment_rate")
    private Double commentRate;
    
    @Column(name = "share_rate")
    private Double shareRate;
    
    // LinkedIn specific metrics
    @Column(name = "profile_views")
    private Long profileViews;
    
    @Column(name = "connection_requests")
    private Long connectionRequests;
    
    @Column(name = "follows")
    private Long follows;
    
    // Content metadata
    @Column(name = "content_type")
    private String contentType;
    
    @Column(name = "content_category")
    private String contentCategory;
    
    @Column(name = "has_media")
    private Boolean hasMedia;
    
    @Column(name = "media_count")
    private Integer mediaCount;
    
    // Raw LinkedIn response for debugging
    @Column(name = "raw_metrics_data", columnDefinition = "TEXT")
    private String rawMetricsData;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
