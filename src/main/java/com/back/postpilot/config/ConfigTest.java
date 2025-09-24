package com.back.postpilot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfigTest implements CommandLineRunner {

    @Value("${spring.ai.azure.openai.api-key:NOT_SET}")
    private String apiKey;

    @Value("${spring.ai.azure.openai.endpoint:NOT_SET}")
    private String endpoint;

    @Value("${spring.ai.azure.openai.chat.options.deployment-name:NOT_SET}")
    private String deploymentName;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Azure OpenAI Configuration Test ===");
        log.info("API Key: {}", apiKey.substring(0, Math.min(apiKey.length(), 10)) + "...");
        log.info("Endpoint: {}", endpoint);
        log.info("Deployment Name: {}", deploymentName);
        log.info("=====================================");
    }
}
