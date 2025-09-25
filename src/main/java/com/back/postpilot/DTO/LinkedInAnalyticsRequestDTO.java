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
public class LinkedInAnalyticsRequestDTO {
    private String postId;
    private String authorId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String timeGranularity; // DAILY, WEEKLY, MONTHLY
    private List<String> metrics; // impressions, clicks, likes, comments, shares, etc.
    private Boolean includeDemographics;
    private Boolean includeTimeSeries;
    private String accessToken;
}
