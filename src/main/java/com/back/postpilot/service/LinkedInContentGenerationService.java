package com.back.postpilot.service;

import com.back.postpilot.EnumTypeConstants.ContentPlatForms;
import com.back.postpilot.EnumTypeConstants.ContentStatus;
import com.back.postpilot.EnumTypeConstants.ContentType;
import com.back.postpilot.domain.ContentGenerationRequest;
import com.back.postpilot.entity.ChatMessage;
import com.back.postpilot.entity.ChatSession;
import com.back.postpilot.entity.GeneratedContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service("linkedinPlatformService")
public class LinkedInContentGenerationService implements PlatformContentGenerationService {

    @Override
    public GeneratedContent generateContent(ContentGenerationRequest request, LLMService llmService, ChatSession chatSession, ChatMessage chatMessage) throws JsonProcessingException {

        String systemPrompt = buildSystemPrompt(request.getContentType());
        String userPrompt = buildUserPrompt(request);


        String rawResponse = llmService.generateRawResponse(systemPrompt + "\n\n" + userPrompt, request);


        String optimizedContent = optimizeForLinkedIn(rawResponse, request.getContentType());
        String hashtags = generateLinkedInHashtags(request.getPrompt(), optimizedContent);
        String metadata = buildLinkedInMetadata(request);

        // 4️⃣ Build GeneratedContent entity
        return GeneratedContent.builder()
                .title("LinkedIn " + request.getContentType().getLabel() + " on: " + request.getPrompt())
                .content(optimizedContent + "\n\n" + hashtags)
                .contentType(request.getContentType())
                .platform(ContentPlatForms.LINKEDIN)
                .status(ContentStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .chatMessage(chatMessage)
                .images(List.of())
                .metadata(metadata)
                .build();
    }

    @Override
    public ContentPlatForms getSupportedPlatform() {
        return ContentPlatForms.LINKEDIN;
    }

    private String buildSystemPrompt(ContentType contentType) {
        return switch (contentType) {
            case POST -> """
                You are a LinkedIn content expert specializing in professional posts.
                Create engaging, professional content that:
                - Uses a conversational yet professional tone
                - Includes relevant industry insights
                - Encourages professional engagement
                - Follows LinkedIn best practices
                - Keeps content between 1000-3000 characters
                """;

            case ARTICLE -> """
                You are a LinkedIn thought leader writing in-depth articles.
                Create comprehensive articles that:
                - Provide valuable insights and actionable advice
                - Use professional formatting with headers and bullet points
                - Include real-world examples and case studies
                - Engage the professional community
                - Are between 1000-2000 words
                """;

            case THREAD, CAROUSEL -> """
                You are creating LinkedIn carousel posts or document threads.
                Create content that:
                - Breaks complex topics into digestible slides
                - Uses clear, professional language
                - Includes actionable takeaways
                - Maintains consistency across slides
                """;

            default -> "You are a LinkedIn content creator focused on professional engagement.";
        };
    }

    private String buildUserPrompt(ContentGenerationRequest request) {
        StringBuilder prompt = new StringBuilder("Create LinkedIn content about: ")
                .append(request.getPrompt());

        if (request.getContext() != null && !request.getContext().isEmpty()) {
            prompt.append("\n\nContext:\n").append(request.getContext());
        }
        if (request.getTargetAudience() != null) {
            prompt.append("\n\nTarget audience: ").append(request.getTargetAudience());
        }
        if (request.getTone() != null) {
            prompt.append("\n\nTone: ").append(request.getTone());
        }

        return prompt.toString();
    }

    private String optimizeForLinkedIn(String content, ContentType contentType) {
        content = addLinkedInFormatting(content);
        content = optimizeHashtagPlacement(content);
        content = ensureCharacterLimit(content, 3000);
        content = addCallToAction(content, contentType);
        return content;
    }

    private String addLinkedInFormatting(String content) {
        return content.replaceAll("(?m)^- ", "• "); // Example: convert dashes to bullet points
    }

    private String optimizeHashtagPlacement(String content) {
        return content + "\n\n"; // Hashtags will be appended later
    }

    private String ensureCharacterLimit(String content, int max) {
        return content.length() > max ? content.substring(0, max - 3) + "..." : content;
    }

    private String addCallToAction(String content, ContentType contentType) {
        if (contentType == ContentType.POST || contentType == ContentType.ARTICLE) {
            return content + "\n\n👉 What are your thoughts? Share in the comments!";
        }
        return content;
    }

    private String generateLinkedInHashtags(String prompt, String content) {
        List<String> keywords = extractKeywords(prompt + " " + content);
        return keywords.stream()
                .limit(5)
                .map(keyword -> "#" + keyword.replaceAll("\\s+", ""))
                .collect(Collectors.joining(" "));
    }

    private List<String> extractKeywords(String text) {
        // Dummy keyword extraction for now
        return Arrays.asList("AI", "LinkedIn", "Innovation", "Tech", "Growth");
    }

    private String buildLinkedInMetadata(ContentGenerationRequest request) throws JsonProcessingException {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("platform_features", List.of("polls", "documents", "videos"));
        metadata.put("optimal_post_time", "Tuesday-Thursday 8AM-10AM");
        metadata.put("engagement_tactics", List.of("ask_questions", "share_insights", "use_storytelling"));
        metadata.put("character_limit", 3000);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(metadata);
    }
}
