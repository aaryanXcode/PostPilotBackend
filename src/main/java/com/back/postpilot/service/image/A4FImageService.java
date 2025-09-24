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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("a4fImageService")
public class A4FImageService implements ImageGenerationService {

    private final RestTemplate restTemplate;

    @Value("${a4f.api.base-url:https://api.a4f.co}")
    private String baseUrl;

    @Value("${a4f.api.key:}")
    private String apiKey;

    @Value("${a4f.api.model:provider-4/imagen-4}")
    private String defaultModelId;

    public A4FImageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ImageGenerationResult generate(ImageGenerationRequest request) {
        try {
            String url = trimTrailingSlash(baseUrl) + "/v1/images/generations";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> payload = new HashMap<>();
            // Always use configured provider-prefixed model id to satisfy A4F requirements
            payload.put("model", defaultModelId);
            payload.put("prompt", request.getPrompt());
            payload.put("n", Math.max(1, request.getNumberOfImages()));
            payload.put("size", (request.getWidth() != null && request.getHeight() != null) ? (request.getWidth() + "x" + request.getHeight()) : "1024x1024");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            Object body = response.getBody();
            List<String> urls = extractUrls(body);
            List<ImageGenerationResult.ImageItem> items = new ArrayList<>();
            for (String u : urls) {
                items.add(ImageGenerationResult.ImageItem.builder()
                        .urlOrBlobKey(u)
                        .mimeType("image/png")
                        .provider("a4f")
                        .model(defaultModelId)
                        .build());
            }
            return ImageGenerationResult.builder().images(items).build();
        } catch (Exception ex) {
            log.error("A4F image generation failed", ex);
            return ImageGenerationResult.builder().images(List.of()).build();
        }
    }

    private List<String> extractUrls(Object body) {
        List<String> urls = new ArrayList<>();
        if (!(body instanceof Map<?, ?> map)) return urls;
        Object data = map.get("data");
        if (data instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> item) {
                    Object url = item.get("url");
                    if (url instanceof String s && !s.isBlank()) {
                        urls.add(s);
                        continue;
                    }
                    Object b64 = item.get("b64_json");
                    if (b64 instanceof String b64s && !b64s.isBlank()) {
                        urls.add("data:image/png;base64," + b64s);
                    }
                }
            }
        }
        return urls;
    }

    private String trimTrailingSlash(String s) {
        if (s == null) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    @Override
    public String getProviderKey() {
        return "a4f";
    }
}


