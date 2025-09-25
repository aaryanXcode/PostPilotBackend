package com.back.postpilot.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostAnalyticsDTO {
    private Long postId;
    private String title;
    private String content;
    private Long engagement;
    private Long reach;
    private Long impressions;
    private Double engagementRate;
    private String status;
    private String platform;
    private String createdAt;
    private String publishedAt;
}

