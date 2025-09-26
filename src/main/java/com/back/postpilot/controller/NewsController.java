package com.back.postpilot.controller;

import com.back.postpilot.DTO.NewsResponseDTO;
import com.back.postpilot.DTO.TechTrendsDTO;
import com.back.postpilot.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/tech")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('USER')")
    public ResponseEntity<NewsResponseDTO> getTechNews(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "us") String country,
            @RequestParam(defaultValue = "en") String language) {
        
        try {
            log.info("=== GET TECH NEWS API CALL STARTED ===");
            log.info("Page: {}, Category: {}, Country: {}, Language: {}", page, category, country, language);
            
            NewsResponseDTO response = newsService.getTechNews(page, category, country, language);
            
            log.info("Tech news fetched successfully. Total results: {}", response.getTotalResults());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching tech news: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(NewsResponseDTO.builder()
                            .status("error")
                            .message("Failed to fetch tech news: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/tech/latest")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('USER')")
    public ResponseEntity<NewsResponseDTO> getLatestTechNews(
            @RequestParam(required = false) Integer limit) {
        
        try {
            log.info("=== GET LATEST TECH NEWS API CALL STARTED ===");
            log.info("Limit: {}", limit);
            
            NewsResponseDTO response = newsService.getLatestTechNews(limit);
            
            log.info("Latest tech news fetched successfully. Total results: {}", response.getTotalResults());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching latest tech news: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(NewsResponseDTO.builder()
                            .status("error")
                            .message("Failed to fetch latest tech news: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/tech/trends")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('USER')")
    public ResponseEntity<List<TechTrendsDTO>> getTechTrends() {
        
        try {
            log.info("=== GET TECH TRENDS API CALL STARTED ===");
            
            List<TechTrendsDTO> trends = newsService.getTechTrends();
            
            log.info("Tech trends fetched successfully. Total trends: {}", trends.size());
            return ResponseEntity.ok(trends);
            
        } catch (Exception e) {
            log.error("Error fetching tech trends: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tech/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('USER')")
    public ResponseEntity<NewsResponseDTO> searchTechNews(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") Integer page) {
        
        try {
            log.info("=== SEARCH TECH NEWS API CALL STARTED ===");
            log.info("Query: {}, Page: {}", query, page);
            
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(NewsResponseDTO.builder()
                                .status("error")
                                .message("Search query cannot be empty")
                                .build());
            }
            
            NewsResponseDTO response = newsService.searchTechNews(query.trim(), page);
            
            log.info("Tech news search completed successfully. Total results: {}", response.getTotalResults());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching tech news: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(NewsResponseDTO.builder()
                            .status("error")
                            .message("Failed to search tech news: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/tech/categories")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('USER')")
    public ResponseEntity<List<String>> getTechCategories() {
        
        try {
            log.info("=== GET TECH CATEGORIES API CALL STARTED ===");
            
            List<String> categories = List.of(
                "technology",
                "science",
                "business",
                "entertainment",
                "sports",
                "health",
                "politics"
            );
            
            log.info("Tech categories fetched successfully. Total categories: {}", categories.size());
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            log.error("Error fetching tech categories: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Object> healthCheck() {
        try {
            log.info("=== NEWS API HEALTH CHECK ===");
            
            // Simple health check by trying to fetch one article
            NewsResponseDTO response = newsService.getLatestTechNews(1);
            
            if ("success".equals(response.getStatus()) || response.getResults() != null) {
                return ResponseEntity.ok().body("News API is healthy");
            } else {
                return ResponseEntity.status(503).body("News API is unhealthy");
            }
            
        } catch (Exception e) {
            log.error("News API health check failed: {}", e.getMessage(), e);
            return ResponseEntity.status(503).body("News API health check failed: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Object> testNewsDataAPI() {
        try {
            log.info("=== TESTING NEWSDATA.IO API ===");
            
            // Test the basic API call
            NewsResponseDTO response = newsService.getLatestTechNews(5);
            
            return ResponseEntity.ok().body(Map.of(
                "status", response.getStatus(),
                "totalResults", response.getTotalResults(),
                "articleCount", response.getResults() != null ? response.getResults().size() : 0,
                "message", response.getMessage(),
                "nextPage", response.getNextPage()
            ));
            
        } catch (Exception e) {
            log.error("NewsData.io API test failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "API test failed",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/public/test")
    public ResponseEntity<Object> publicTestNewsDataAPI() {
        try {
            log.info("=== PUBLIC TESTING NEWSDATA.IO API ===");
            
            // Test the basic API call without authentication
            NewsResponseDTO response = newsService.getLatestTechNews(3);
            
            return ResponseEntity.ok().body(Map.of(
                "status", response.getStatus(),
                "totalResults", response.getTotalResults(),
                "articleCount", response.getResults() != null ? response.getResults().size() : 0,
                "message", response.getMessage(),
                "nextPage", response.getNextPage(),
                "test", "Public endpoint working"
            ));
            
        } catch (Exception e) {
            log.error("Public NewsData.io API test failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Public API test failed",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/public/simple")
    public ResponseEntity<Object> simpleNewsTest() {
        try {
            log.info("=== SIMPLE NEWSDATA.IO API TEST ===");
            
            // Test with minimal parameters - just the basic API call
            NewsResponseDTO response = newsService.getTechNews(0, "technology", "us", "en");
            
            return ResponseEntity.ok().body(Map.of(
                "status", response.getStatus(),
                "totalResults", response.getTotalResults(),
                "articleCount", response.getResults() != null ? response.getResults().size() : 0,
                "message", response.getMessage(),
                "nextPage", response.getNextPage(),
                "test", "Simple test working"
            ));
            
        } catch (Exception e) {
            log.error("Simple NewsData.io API test failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Simple API test failed",
                "message", e.getMessage()
            ));
        }
    }
}
