package com.back.postpilot.service;

import com.back.postpilot.EnumTypeConstants.ContentPlatForms;
import com.back.postpilot.domain.ContentGenerationRequest;
import com.back.postpilot.entity.ChatMessage;
import com.back.postpilot.entity.ChatSession;
import com.back.postpilot.entity.GeneratedContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

@Service("defaultPlatformService")
public class DefaultContentGenerationService implements PlatformContentGenerationService{
    @Override
    public GeneratedContent generateContent(ContentGenerationRequest request, LLMService llmService, ChatSession chatSession, ChatMessage chatMessage) {
        return null;
    }

    @Override
    public ContentPlatForms getSupportedPlatform() {
        return ContentPlatForms.DEFAULT;
    }

}
