package com.back.postpilot.service.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service("bytezImageService")
public class BytezImageService implements ImageGenerationService {

    private final RestTemplate restTemplate;

    @Value("${bytez.api.base-url:https://api.bytez.com}")
    private String baseUrl;

    @Value("${bytez.api.model-path:/models/v2/dreamlike-art/dreamlike-photoreal-2.0}")
    private String modelPath;

    @Value("${bytez.api.key:}")
    private String apiKey;

    public BytezImageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ImageGenerationResult generate(ImageGenerationRequest request) {
        try {
            String url = trimTrailingSlash(baseUrl) + ensureLeadingSlash(modelPath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Key " + apiKey);

            Map<String, Object> payload = Map.of(
                    "text", request.getPrompt()
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            Object body = response.getBody();
            String imageUrl = extractImageUrl(body);
            if (imageUrl == null) {
                log.warn("Bytez response did not contain a recognizable image url. body={}", body);
                return ImageGenerationResult.builder().images(List.of()).build();
            }

            ImageGenerationResult.ImageItem item = ImageGenerationResult.ImageItem.builder()
                    .urlOrBlobKey(imageUrl)
                    .mimeType("image/png")
                    .provider("bytez")
                    .model(modelPath)
                    .build();
            return ImageGenerationResult.builder().images(List.of(item)).build();
        } catch (Exception ex) {
            log.error("Bytez image generation failed", ex);
            return ImageGenerationResult.builder().images(List.of()).build();
        }
    }

    private String extractImageUrl(Object body) {
        if (!(body instanceof Map<?, ?> map)) return null;

        // Try common fields
        Object url = map.get("url");
        if (url instanceof String s && !s.isBlank()) return s;

        Object imageUrl = map.get("image_url");
        if (imageUrl instanceof String s2 && !s2.isBlank()) return s2;

        // Bytez sample response contains 'output' with a direct CDN URL
        Object output = map.get("output");
        if (output instanceof String s3 && !s3.isBlank()) return s3;

        Object data = map.get("data");
        if (data instanceof String b64 && !b64.isBlank()) {
            return "data:image/png;base64," + b64;
        }

        // Some APIs wrap results
        Object result = map.get("result");
        if (result instanceof Map<?, ?> inner) {
            Object innerUrl = inner.get("url");
            if (innerUrl instanceof String s3 && !s3.isBlank()) return s3;
        }

        return null;
    }

    private String trimTrailingSlash(String s) {
        if (s == null) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    private String ensureLeadingSlash(String s) {
        if (s == null || s.isBlank()) return "";
        return s.startsWith("/") ? s : "/" + s;
    }

    @Override
    public String getProviderKey() {
        return "bytez";
    }
}


