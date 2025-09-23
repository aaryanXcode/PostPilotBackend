package com.back.postpilot.service;

import com.back.postpilot.DTO.GenerateContentDTO;
import com.back.postpilot.EnumTypeConstants.ContentPlatForms;
import com.back.postpilot.controller.LinkedInOAuthController;
import com.back.postpilot.entity.GeneratedContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.back.postpilot.controller.LinkedInOAuthController.token;
import static com.back.postpilot.linkedInAuth.Constants.USER_AGENT_OAUTH_VALUE;

@Component
public class LinkedInContentPostService implements PlatformContentPostService{

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(LinkedInContentPostService.class);

    private final LinkedInUserService linkedInUserService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate; // Add RestTemplate


    private static final String UGC_POSTS_ENDPOINT = "https://api.linkedin.com/v2/ugcPosts";

    public LinkedInContentPostService(LinkedInUserService linkedInUserService, RestTemplate restTemplate) {
        this.linkedInUserService = linkedInUserService;
        this.restTemplate = restTemplate; // Inject RestTemplate
    }


    @Override
    public String postContent(GenerateContentDTO generateContentDTO) throws Exception { // Changed return type

        String profile = linkedInUserService.getProfile();
        String userId = objectMapper.readTree(profile).get("sub").asText(); // ✅ Fixed: use "sub"
        log.debug("Posting content on LinkedIn for user: {}", userId);

        Map<String, Object> postData = getStringObjectMap(generateContentDTO, userId);

        // Convert to JSON
        String jsonPayload = objectMapper.writeValueAsString(postData);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT_OAUTH_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

        // ✅ Actually POST the content
        try {
            String response = restTemplate.postForObject(UGC_POSTS_ENDPOINT, request, String.class);
            log.info("Successfully posted to LinkedIn: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error posting to LinkedIn: {}", e.getMessage());
            throw e;
        }
    }

    @NotNull
    private static Map<String, Object> getStringObjectMap(GenerateContentDTO generateContentDTO, String userId) {
        Map<String, Object> postData = new HashMap<>();
        postData.put("author", "urn:li:person:" + userId);
        postData.put("lifecycleState", "PUBLISHED");

        // Share content
        Map<String, Object> shareContent = new HashMap<>();
        Map<String, Object> shareCommentary = new HashMap<>();
        shareCommentary.put("text", generateContentDTO.content());
        shareContent.put("shareCommentary", shareCommentary);
        shareContent.put("shareMediaCategory", "NONE");

        Map<String, Object> specificContent = new HashMap<>();
        specificContent.put("com.linkedin.ugc.ShareContent", shareContent);
        postData.put("specificContent", specificContent);

        // Visibility settings
        Map<String, Object> visibility = new HashMap<>();
        visibility.put("com.linkedin.ugc.MemberNetworkVisibility", "PUBLIC");
        postData.put("visibility", visibility);
        return postData;
    }

    /**
     * Create a LinkedIn post with text and media
     * @param accessToken the access token
     * @param userId the user's LinkedIn ID
     * @param postText the text content to post
     * @param mediaUrn the URN of the uploaded media
     * @return HTTP entity for the post request
     */
    public HttpEntity<String> createMediaPost(String accessToken, String userId, String postText, String mediaUrn) throws Exception {

        // Build the post payload
        Map<String, Object> postData = new HashMap<>();
        postData.put("author", "urn:li:person:" + userId);
        postData.put("lifecycleState", "PUBLISHED");

        // Share content with media
        Map<String, Object> shareContent = new HashMap<>();
        Map<String, Object> shareCommentary = new HashMap<>();
        shareCommentary.put("text", postText);
        shareContent.put("shareCommentary", shareCommentary);
        shareContent.put("shareMediaCategory", "IMAGE");

        // Media reference
        Map<String, Object> media = new HashMap<>();
        media.put("status", "READY");
        media.put("description", new HashMap<String, Object>() {{
            put("text", postText);
        }});
        media.put("media", mediaUrn);
        shareContent.put("media", new Object[]{media});

        Map<String, Object> specificContent = new HashMap<>();
        specificContent.put("com.linkedin.ugc.ShareContent", shareContent);
        postData.put("specificContent", specificContent);

        // Visibility settings
        Map<String, Object> visibility = new HashMap<>();
        visibility.put("com.linkedin.ugc.MemberNetworkVisibility", "PUBLIC");
        postData.put("visibility", visibility);

        // Convert to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(postData);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT_OAUTH_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        return new HttpEntity<>(jsonPayload, headers);
    }

    @Override
    public ContentPlatForms getSupportedPlatform() {
        return ContentPlatForms.LINKEDIN;
    }
}
