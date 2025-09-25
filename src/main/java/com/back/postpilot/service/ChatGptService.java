package com.back.postpilot.service;

import com.back.postpilot.domain.ContentGenerationRequest;
import com.back.postpilot.entity.ChatMessage;
import com.back.postpilot.entity.ChatSession;
import com.back.postpilot.EnumTypeConstants.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;

@Slf4j
@Service("chatgptService")
public class ChatGptService implements LLMService {

    private final ChatClient azureOpenAiChatClient;

    public ChatGptService(@Qualifier("AzureOpenAiChatClient") ChatClient azureOpenAiChatClient) {
        this.azureOpenAiChatClient = azureOpenAiChatClient;
    }

    @Override
    public ChatMessage generateChatMessage(String prompt, ChatSession session, ContentGenerationRequest request) {
        ChatMessage aiMessage = new ChatMessage();

        if (session.getTitle() == null || session.getTitle().isEmpty()) {
            String titlePrompt = "Generate a short, descriptive title (max 50 characters) for this query:\n\n"
                    + prompt +
                    "\n\nReturn ONLY the title text, no explanation, no formatting.";
            session.setTitle(chatResponse(titlePrompt));
        }

        if (!request.getPlatform().equalsIgnoreCase("default")) {
            prompt += "only summarize this Because we are going to another detailed response below";
        }

        String aiResponse = chatResponse(prompt);
        session.setUpdatedAt(LocalDateTime.now());
        aiMessage.setContent(aiResponse);
        aiMessage.setMessageType(MessageType.ASSISTANT);
        aiMessage.setChatSession(session);
        aiMessage.setTimestamp(LocalDateTime.now());
        return aiMessage;
    }

    @Override
    public String generateRawResponse(String prompt, ContentGenerationRequest request){
        return chatResponse(prompt);
    }

    private String chatResponse(String prompt) {
        try {
            if (prompt == null || prompt.trim().isEmpty()) {
                return "Prompt cannot be empty";
            }
            
            log.debug("Calling Azure OpenAI with prompt: {}", prompt);
            String response = azureOpenAiChatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
            log.debug("Azure OpenAI response: {}", response);
            return response;
        } catch (Exception ex) {
            log.error("Error calling Azure OpenAI: ", ex);
            return "bad Request";
        }
    }
}
