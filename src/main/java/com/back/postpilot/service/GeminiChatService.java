package com.back.postpilot.service;

import com.back.postpilot.EnumTypeConstants.MessageType;
import com.back.postpilot.domain.ContentGenerationRequest;
import com.back.postpilot.entity.ChatMessage;
import com.back.postpilot.entity.ChatSession;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service("geminiService")
public class GeminiChatService implements LLMService {

    @Override
    public ChatMessage generateChatMessage(String prompt, ChatSession session, ContentGenerationRequest request) {
        ChatMessage aiMessage = new ChatMessage();
        if(session.getTitle()==null || session.getTitle().isEmpty()){
            session.setTitle(chatResponse("Generate a short, descriptive title (max 50 characters) for this query:\n\n"
                            + prompt +
                            "\n\nReturn ONLY the title text, no explanation, no formatting."
                    , request.getModel()));
        }
        if(!request.getPlatform().equalsIgnoreCase("default")){
            prompt += "only summarize this Because we are going to another detailed response below";
        }
        String aiResponse = chatResponse(prompt, request.getModel());
        session.setUpdatedAt(LocalDateTime.now());
        aiMessage.setContent(aiResponse);
        aiMessage.setMessageType(MessageType.ASSISTANT);
        aiMessage.setChatSession(session);
        aiMessage.setTimestamp(LocalDateTime.now());
        return aiMessage;
    }

    public String chatResponse(String prompt, String model) {
        try{
            Client client = new Client();
            if (prompt == null || prompt.trim().isEmpty()) {
                return "Prompt cannot be empty";
            }
            return client.models.generateContent(model, prompt, null).text();
        } catch(Exception ex){
            log.debug(ex.getMessage());
            return "bad Request";
        }
    }

    public String generateRawResponse(String prompt, ContentGenerationRequest request){
        String model = request.getModel();
        try{
            Client client = new Client();
            if (prompt == null || prompt.trim().isEmpty()) {
                return "Prompt cannot be empty";
            }
            return client.models.generateContent(model, prompt, null).text();
        } catch(Exception ex){
            log.debug(ex.getMessage());
            return "bad Request";
        }
    }


}
