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
public class NewsResponseDTO {
    
    private String status;
    private Integer totalResults;
    private List<NewsArticleDTO> results;
    private String nextPage;
    private String previousPage;
    private String message;
}
