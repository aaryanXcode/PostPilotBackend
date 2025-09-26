package com.back.postpilot.service;

import com.back.postpilot.DTO.NewsArticleDTO;
import com.back.postpilot.DTO.NewsResponseDTO;
import com.back.postpilot.DTO.TechTrendsDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${newsdata.api.key}")
    private String apiKey;

    @Value("${newsdata.api.base-url:https://newsdata.io/api/1}")
    private String baseUrl;

    public NewsResponseDTO getTechNews(Integer page, String category, String country, String language) {
        log.info("=== FETCHING TECH NEWS ===");
        log.info("Page: {}, Category: {}, Country: {}, Language: {}", page, category, country, language);

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/news")
                    .queryParam("apikey", apiKey)
                    .queryParam("category", category != null ? category : "technology")
                    .queryParam("country", country != null ? country : "us")
                    .queryParam("language", language != null ? language : "en");
            
            String url = builder.toUriString();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
            
            return parseNewsResponse(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching tech news: {}", e.getMessage(), e);
            return NewsResponseDTO.builder()
                    .status("error")
                    .message("Failed to fetch tech news: " + e.getMessage())
                    .results(new ArrayList<>())
                    .build();
        }
    }

    public NewsResponseDTO getLatestTechNews(Integer limit) {
        log.info("=== FETCHING LATEST TECH NEWS ===");
        log.info("Limit: {}", limit);

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/news")
                    .queryParam("apikey", apiKey)
                    .queryParam("category", "technology")
                    .queryParam("country", "us")
                    .queryParam("language", "en");
            
            // NewsData.io doesn't support size parameter, we'll limit results in parsing
            String url = builder.toUriString();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
            
            NewsResponseDTO result = parseNewsResponse(response.getBody());
            
            // Apply limit after parsing if specified
            if (limit != null && limit > 0 && result.getResults() != null && result.getResults().size() > limit) {
                result.setResults(result.getResults().subList(0, limit));
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error fetching latest tech news: {}", e.getMessage(), e);
            return NewsResponseDTO.builder()
                    .status("error")
                    .message("Failed to fetch latest tech news: " + e.getMessage())
                    .results(new ArrayList<>())
                    .build();
        }
    }

    public List<TechTrendsDTO> getTechTrends() {
        log.info("=== FETCHING TECH TRENDS ===");

        try {
            // Get trending tech news
            NewsResponseDTO newsResponse = getLatestTechNews(20);
            
            if (newsResponse.getResults() == null || newsResponse.getResults().isEmpty()) {
                return new ArrayList<>();
            }

            // Analyze trends from the news data
            return analyzeTrends(newsResponse.getResults());
        } catch (Exception e) {
            log.error("Error fetching tech trends: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public NewsResponseDTO searchTechNews(String query, Integer page) {
        log.info("=== SEARCHING TECH NEWS ===");
        log.info("Query: {}, Page: {}", query, page);

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + "/news")
                    .queryParam("apikey", apiKey)
                    .queryParam("category", "technology")
                    .queryParam("country", "us")
                    .queryParam("language", "en")
                    .queryParam("q", query);
            
            String url = builder.toUriString();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
            
            return parseNewsResponse(response.getBody());
        } catch (Exception e) {
            log.error("Error searching tech news: {}", e.getMessage(), e);
            return NewsResponseDTO.builder()
                    .status("error")
                    .message("Failed to search tech news: " + e.getMessage())
                    .results(new ArrayList<>())
                    .build();
        }
    }


    private NewsResponseDTO parseNewsResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            List<NewsArticleDTO> articles = new ArrayList<>();
            JsonNode results = root.get("results");
            
            if (results != null && results.isArray()) {
                for (JsonNode article : results) {
                    NewsArticleDTO articleDTO = NewsArticleDTO.builder()
                            .articleId(article.get("article_id") != null ? article.get("article_id").asText() : null)
                            .title(article.get("title") != null ? article.get("title").asText() : null)
                            .link(article.get("link") != null ? article.get("link").asText() : null)
                            .description(article.get("description") != null ? article.get("description").asText() : null)
                            .content(article.get("content") != null ? article.get("content").asText() : null)
                            .imageUrl(article.get("image_url") != null ? article.get("image_url").asText() : null)
                            .sourceId(article.get("source_id") != null ? article.get("source_id").asText() : null)
                            .sourceName(article.get("source_name") != null ? article.get("source_name").asText() : null)
                            .sourceUrl(article.get("source_url") != null ? article.get("source_url").asText() : null)
                            .language(article.get("language") != null ? article.get("language").asText() : null)
                            .country(article.get("country") != null ? article.get("country").asText() : null)
                            .category(article.get("category") != null ? article.get("category").asText() : null)
                            .author(article.get("creator") != null && article.get("creator").isArray() ? 
                                    article.get("creator").get(0).asText() : null)
                            .videoUrl(article.get("video_url") != null ? article.get("video_url").asText() : null)
                            .pubDate(parseDateTime(article.get("pubDate") != null ? article.get("pubDate").asText() : null))
                            .build();

                    // Parse keywords if available
                    if (article.get("keywords") != null && article.get("keywords").isArray()) {
                        List<String> keywords = new ArrayList<>();
                        for (JsonNode keyword : article.get("keywords")) {
                            keywords.add(keyword.asText());
                        }
                        articleDTO.setKeywords(keywords);
                    }

                    articles.add(articleDTO);
                }
            }

            return NewsResponseDTO.builder()
                    .status(root.get("status") != null ? root.get("status").asText() : "success")
                    .totalResults(root.get("totalResults") != null ? root.get("totalResults").asInt() : articles.size())
                    .results(articles)
                    .nextPage(root.get("nextPage") != null ? root.get("nextPage").asText() : null)
                    .message("Successfully fetched tech news")
                    .build();

        } catch (Exception e) {
            log.error("Error parsing news response: {}", e.getMessage(), e);
            return NewsResponseDTO.builder()
                    .status("error")
                    .message("Failed to parse news response: " + e.getMessage())
                    .results(new ArrayList<>())
                    .build();
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            // Try different date formats
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDateTime.parse(dateTimeStr, formatter);
                } catch (Exception ignored) {
                    // Try next formatter
                }
            }
        } catch (Exception e) {
            log.warn("Could not parse date: {}", dateTimeStr);
        }
        return null;
    }

    private List<TechTrendsDTO> analyzeTrends(List<NewsArticleDTO> articles) {
        // Simple trend analysis based on keywords and categories
        List<TechTrendsDTO> trends = new ArrayList<>();
        
        // Group articles by category
        articles.stream()
                .collect(Collectors.groupingBy(NewsArticleDTO::getCategory))
                .forEach((category, categoryArticles) -> {
                    if (category != null && !category.isEmpty()) {
                        List<String> keywords = categoryArticles.stream()
                                .flatMap(article -> article.getKeywords() != null ? article.getKeywords().stream() : Arrays.stream(new String[0]))
                                .distinct()
                                .limit(5)
                                .collect(Collectors.toList());

                        TechTrendsDTO trend = TechTrendsDTO.builder()
                                .trendId("trend_" + category.toLowerCase().replace(" ", "_"))
                                .title(category + " Tech Trends")
                                .description("Latest trends in " + category + " technology")
                                .category(category)
                                .articleCount(categoryArticles.size())
                                .topKeywords(keywords)
                                .lastUpdated(LocalDateTime.now())
                                .trendScore(String.valueOf(categoryArticles.size()))
                                .relatedArticles(categoryArticles.stream().limit(3).collect(Collectors.toList()))
                                .build();

                        trends.add(trend);
                    }
                });

        return trends;
    }
}
