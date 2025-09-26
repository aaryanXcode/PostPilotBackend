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
public class TechTrendsDTO {
    
    private String trendId;
    private String title;
    private String description;
    private String category;
    private Integer articleCount;
    private List<String> topKeywords;
    private LocalDateTime lastUpdated;
    private String trendScore;
    private List<NewsArticleDTO> relatedArticles;
}
