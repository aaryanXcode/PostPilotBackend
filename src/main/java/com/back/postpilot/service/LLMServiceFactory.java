package com.back.postpilot.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.back.postpilot.EnumTypeConstants.AssitanceModels;


@Component
public class LLMServiceFactory {

    @Autowired
    private ApplicationContext context;

    public LLMService getService(AssitanceModels model) {
        return switch (model) {
            case CHATGPT -> context.getBean("chatgptService", LLMService.class);
            case GEMINI -> context.getBean("geminiService", LLMService.class);
            case CLAUDE -> context.getBean("claudeService", LLMService.class);
            case OLLAMA -> context.getBean("ollamaService", LLMService.class);
        };
    }
}
