package com.back.postpilot.controller;

import com.back.postpilot.DTO.ModelInfoDTO;
import com.back.postpilot.DTO.PlatformInfoDTO;
import com.back.postpilot.EnumTypeConstants.AssitanceModels;
import com.back.postpilot.EnumTypeConstants.ContentPlatForms;
import com.back.postpilot.entity.SocialMediaPlatForm;
import com.back.postpilot.repository.SocialMediaPlatFormRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/supported")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SupportedModelsController {

    private final SocialMediaPlatFormRepository socialMediaPlatFormRepository;

    @Value("${spring.ai.ollama.chat.enabled:false}")
    private boolean ollamaEnabled;

    @Value("${spring.ai.openai.chat.enabled:false}")
    private boolean openaiEnabled;

    @Value("${spring.ai.azure.openai.chat.enabled:false}")
    private boolean azureOpenaiEnabled;

    @Value("${spring.ai.vertex.ai.gemini.chat.enabled:true}")
    private boolean geminiEnabled;

    @Value("${spring.ai.anthropic.chat.enabled:false}")
    private boolean anthropicEnabled;

    /**
     * Get all active supported AI models based on configuration
     * 
     * @return List of active AI models with their details
     */
    @GetMapping("/models")
    public ResponseEntity<List<ModelInfoDTO>> getActiveModels() {
        try {
            log.info("Fetching active AI models");
            
            List<ModelInfoDTO> activeModels = new ArrayList<>();
            
            // Check each model based on configuration
            for (AssitanceModels model : AssitanceModels.values()) {
                boolean isActive = isModelActive(model);
                if (isActive) {
                    ModelInfoDTO modelInfo = ModelInfoDTO.builder()
                            .key(model.getKey())
                            .name(model.name())
                            .displayName(getModelDisplayName(model))
                            .description(getModelDescription(model))
                            .active(true)
                            .provider(getModelProvider(model))
                            .status("available")
                            .build();
                    
                    activeModels.add(modelInfo);
                }
            }
            
            log.info("Found {} active models", activeModels.size());
            return ResponseEntity.ok(activeModels);
            
        } catch (Exception e) {
            log.error("Error fetching active models", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all supported content platforms
     * 
     * @return List of supported content platforms
     */
    @GetMapping("/platforms")
    public ResponseEntity<List<PlatformInfoDTO>> getSupportedPlatforms() {
        try {
            log.info("Fetching supported content platforms");
            
            List<PlatformInfoDTO> platforms = new ArrayList<>();
            
            // Get platforms from enum
            for (ContentPlatForms platform : ContentPlatForms.values()) {
                PlatformInfoDTO platformInfo = PlatformInfoDTO.builder()
                        .key(platform.name())
                        .name(platform.getPlatform())
                        .displayName(platform.getPlatform())
                        .description(getPlatformDescription(platform))
                        .active(true)
                        .category(getPlatformCategory(platform))
                        .build();
                
                platforms.add(platformInfo);
            }
            
            log.info("Found {} supported platforms", platforms.size());
            return ResponseEntity.ok(platforms);
            
        } catch (Exception e) {
            log.error("Error fetching supported platforms", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all content platforms from database (if any are stored)
     * 
     * @return List of content platforms from database
     */
    @GetMapping("/platforms/database")
    public ResponseEntity<List<SocialMediaPlatForm>> getPlatformsFromDatabase() {
        try {
            log.info("Fetching content platforms from database");
            
            List<SocialMediaPlatForm> platforms = socialMediaPlatFormRepository.findAll();
            
            log.info("Found {} platforms in database", platforms.size());
            return ResponseEntity.ok(platforms);
            
        } catch (Exception e) {
            log.error("Error fetching platforms from database", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all available models (both active and inactive)
     * 
     * @return List of all available models with their status
     */
    @GetMapping("/models/all")
    public ResponseEntity<List<ModelInfoDTO>> getAllModels() {
        try {
            log.info("Fetching all available models");
            
            List<ModelInfoDTO> allModels = new ArrayList<>();
            
            for (AssitanceModels model : AssitanceModels.values()) {
                boolean isActive = isModelActive(model);
                ModelInfoDTO modelInfo = ModelInfoDTO.builder()
                        .key(model.getKey())
                        .name(model.name())
                        .displayName(getModelDisplayName(model))
                        .description(getModelDescription(model))
                        .active(isActive)
                        .provider(getModelProvider(model))
                        .status(isActive ? "available" : "unavailable")
                        .build();
                
                allModels.add(modelInfo);
            }
            
            log.info("Found {} total models", allModels.size());
            return ResponseEntity.ok(allModels);
            
        } catch (Exception e) {
            log.error("Error fetching all models", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if a specific model is active
     * 
     * @param modelKey Model key to check
     * @return Model status information
     */
    @GetMapping("/models/{modelKey}/status")
    public ResponseEntity<ModelInfoDTO> getModelStatus(@PathVariable String modelKey) {
        try {
            log.info("Checking status for model: {}", modelKey);
            
            AssitanceModels model = AssitanceModels.fromString(modelKey);
            boolean isActive = isModelActive(model);
            
            ModelInfoDTO status = ModelInfoDTO.builder()
                    .key(model.getKey())
                    .name(model.name())
                    .displayName(getModelDisplayName(model))
                    .description(getModelDescription(model))
                    .active(isActive)
                    .provider(getModelProvider(model))
                    .status(isActive ? "available" : "unavailable")
                    .build();
            
            return ResponseEntity.ok(status);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid model key: {}", modelKey);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error checking model status for {}", modelKey, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Helper methods
    private boolean isModelActive(AssitanceModels model) {
        return switch (model) {
            case CHATGPT -> azureOpenaiEnabled || openaiEnabled;
            case GEMINI -> geminiEnabled;
            case CLAUDE -> anthropicEnabled;
            case OLLAMA -> ollamaEnabled;
        };
    }

    private String getModelDisplayName(AssitanceModels model) {
        return switch (model) {
            case CHATGPT -> "ChatGPT (OpenAI)";
            case GEMINI -> "Gemini (Google)";
            case CLAUDE -> "Claude (Anthropic)";
            case OLLAMA -> "Ollama (Local)";
        };
    }

    private String getModelDescription(AssitanceModels model) {
        return switch (model) {
            case CHATGPT -> "OpenAI's advanced language model with Azure OpenAI support";
            case GEMINI -> "Google's multimodal AI model via Vertex AI";
            case CLAUDE -> "Anthropic's conversational AI assistant";
            case OLLAMA -> "Local AI models running on your machine";
        };
    }

    private String getModelProvider(AssitanceModels model) {
        return switch (model) {
            case CHATGPT -> "OpenAI/Azure";
            case GEMINI -> "Google";
            case CLAUDE -> "Anthropic";
            case OLLAMA -> "Ollama";
        };
    }

    private String getPlatformDescription(ContentPlatForms platform) {
        return switch (platform) {
            case INSTAGRAM -> "Visual content platform for photos and videos";
            case FACEBOOK -> "Social networking platform for various content types";
            case LINKEDIN -> "Professional networking platform for business content";
            case TWITTER -> "Microblogging platform for short-form content";
            case DEFAULT -> "Generic content platform";
        };
    }

    private String getPlatformCategory(ContentPlatForms platform) {
        return switch (platform) {
            case INSTAGRAM -> "visual";
            case FACEBOOK -> "social";
            case LINKEDIN -> "professional";
            case TWITTER -> "microblogging";
            case DEFAULT -> "generic";
        };
    }
}
