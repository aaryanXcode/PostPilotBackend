package com.back.postpilot.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class NewsArticleDTO {
    
    private String articleId;
    private String title;
    private String link;
    private String description;
    private String content;
    private String imageUrl;
    private String sourceId;
    private String sourceName;
    private String sourceUrl;
    private String language;
    private String country;
    private String category;
    private List<String> keywords;
    private LocalDateTime pubDate;
    private String author;
    private String videoUrl;
    private String fullDescription;
    
    // NewsData.io specific fields
    @JsonProperty("creator")
    private List<String> creators;
    
    @JsonProperty("video_url")
    private String videoUrlField;
    
    @JsonProperty("image_url")
    private String imageUrlField;
    
    @JsonProperty("source_priority")
    private Integer sourcePriority;
    
    @JsonProperty("source_icon")
    private String sourceIcon;
}
