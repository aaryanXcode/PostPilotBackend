package com.back.postpilot.service;

import com.back.postpilot.domain.ContentGenerationRequest;
import com.back.postpilot.entity.ChatMessage;
import com.back.postpilot.entity.ChatSession;
import com.google.genai.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service("chatgptService")
public class ChatGptService implements LLMService {

    @Override
    public ChatMessage generateChatMessage(String prompt, ChatSession session, ContentGenerationRequest request) {
        // Call OpenAI API here
        // Example payload (simplified)
        // do HTTP call with RestTemplate or WebClient
        return null;
    }

    @Override
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
