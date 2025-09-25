package com.back.postpilot.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDTO {
    private Long totalPosts;
    private Long generatedPostsCount;
    private Long publishedPostsCount;
    private Long scheduledPostsCount;
    private Long draftPostsCount;
    private Long totalEngagement;
    private Long totalReach;
    private Long totalImpressions;
    private Double avgEngagementRate;
    private Long bestPostEngagement;
    private Long totalFollowers;
    private List<RecentPostDTO> recentPosts;
}
