package com.back.postpilot.service;

import com.back.postpilot.domain.ContentGenerationRequest;
import com.back.postpilot.entity.ChatMessage;
import com.back.postpilot.entity.ChatSession;

public interface LLMService {
    ChatMessage generateChatMessage(String prompt, ChatSession session, ContentGenerationRequest request);
    String generateRawResponse(String prompt, ContentGenerationRequest request);
}
