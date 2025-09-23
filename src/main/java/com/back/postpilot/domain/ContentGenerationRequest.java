package com.back.postpilot.domain;

import com.back.postpilot.EnumTypeConstants.ContentType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class ContentGenerationRequest {
    private Long userId;
    private String sessionId;
    private String model;
    private String platform;
    private String prompt;
    private String context;
    private ContentType contentType;
    private String targetAudience;
    private String tone;
    private Map<String, Object> additionalParameters;
    private int maxCharacters;
}
