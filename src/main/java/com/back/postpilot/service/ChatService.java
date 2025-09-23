package com.back.postpilot.service;

import com.back.postpilot.DTO.ChatMessageDTO;
import com.back.postpilot.DTO.GenerateContentDTO;
import com.back.postpilot.EnumTypeConstants.*;
import com.back.postpilot.domain.ContentGenerationRequest;
import com.back.postpilot.domain.PlatformContentGenerator;
import com.back.postpilot.entity.ChatMessage;
import com.back.postpilot.entity.ChatSession;
import com.back.postpilot.entity.ContentImage;
import com.back.postpilot.entity.GeneratedContent;
import com.back.postpilot.repository.ChatMessageRepository;
import com.back.postpilot.repository.ChatSessionRepository;
import com.back.postpilot.repository.GeneratedContentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.genai.Client;
import com.google.genai.types.GenerateImagesConfig;
import com.google.genai.types.GenerateImagesResponse;
import com.google.genai.types.GeneratedImage;
import com.google.genai.types.Image;
import jakarta.transaction.Transactional;
import jakarta.xml.bind.annotation.XmlType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GeneratedContentRepository generatedContentRepository;
    private final PlatformContentGenerationServiceFactory platformContentGenerationServiceFactory;
    private final LLMServiceFactory llmServiceFactory;
    //private final PlatformContentGenerator platformContentGenerator;

    public ChatService(ChatSessionRepository chatSessionRepository,
                       ChatMessageRepository chatMessageRepository,
                       PlatformContentGenerationServiceFactory platformContentGenerationServiceFactory,
                       LLMServiceFactory llmServiceFactory,
                       GeneratedContentRepository generatedContentRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.platformContentGenerationServiceFactory = platformContentGenerationServiceFactory;
        this.llmServiceFactory = llmServiceFactory;
        this.generatedContentRepository = generatedContentRepository;
        //this.platformContentGenerator = platformContentGenerator;
    }

    public ChatMessageDTO getResponse(ContentGenerationRequest request) throws JsonProcessingException {
        // 1️⃣ Find or create session
        ChatSession session = chatSessionRepository.findBySessionId(request.getSessionId())
                .orElseGet(() -> {
                    ChatSession newSession = new ChatSession();
                    newSession.setUserId(request.getUserId());
                    newSession.setSessionId(UUID.randomUUID().toString());
                    newSession.setStatus(ChatStatus.ACTIVE);
                    newSession.setCreatedAt(LocalDateTime.now());
                    return chatSessionRepository.save(newSession);
                });

        // 2️⃣ Save user message
        ChatMessage userMessage = new ChatMessage();
        userMessage.setContent(request.getPrompt());
        userMessage.setMessageType(MessageType.USER);
        userMessage.setChatSession(session);
        userMessage.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(userMessage);

        // Map user message to DTO (optional, if needed for logging/response)
        ChatMessageDTO userMessageDTO = new ChatMessageDTO.Builder()
                .id(userMessage.getId())
                .sessionId(session.getSessionId())
                .sender("USER")
                .content(userMessage.getContent())
                .messageType(userMessage.getMessageType())
                .timestamp(userMessage.getTimestamp())
                .sequenceNumber(session.getMessages().size() + 1) // example sequence
                .build();



        // 3️⃣ Get LLMService from factory
        LLMService llmService = llmServiceFactory.getService(AssitanceModels.fromString(request.getModel()));

        // 4️⃣ Call LLM for AI response
        ChatMessage aiResponse = llmService.generateChatMessage(request.getPrompt(), session, request);

        //get service of social media platform
        ContentPlatForms platform = ContentPlatForms.valueOf(request.getPlatform().toUpperCase());
        PlatformContentGenerationService platformContentGenerationService = platformContentGenerationServiceFactory.getPlatformService(platform);
        GeneratedContent generatedContent = platformContentGenerationService.generateContent(request, llmService, session, aiResponse);


        //image if required

        //save content
        GenerateContentDTO generatedContentDTO = null;
        if(generatedContent!=null) {
            GeneratedContent savedContent = generatedContentRepository.save(generatedContent);

            //add in chatMessageDTO
             generatedContentDTO = GenerateContentDTO.builder()
                    .id(savedContent.getId())
                    .title(savedContent.getTitle())
                     .platform(String.valueOf(savedContent.getPlatform()))
                    .content(savedContent.getContent())
                    .contentType(savedContent.getContentType())
                    .imageUrls(savedContent.getImages())
                    .hashtags(savedContent.getHashtags())
                    .metadata(savedContent.getMetadata())
                    .createdAt(savedContent.getCreatedAt())
                    .status(savedContent.getStatus())
                    .build();
        }




        // 5️⃣ Save AI message
        chatMessageRepository.save(aiResponse);

        // 6️⃣ Map AI message to DTO and return

        return new ChatMessageDTO.Builder()
                .id(aiResponse.getId())
                .title(session.getTitle())
                .sessionId(session.getSessionId())
                .sender(MessageType.ASSISTANT.toString())
                .content(aiResponse.getContent())
                .messageType(aiResponse.getMessageType())
                .timestamp(aiResponse.getTimestamp())
                .sequenceNumber(session.getMessages().size() + 1)
                .modelType(AssitanceModels.fromString(request.getModel()))
                .generateContentDTO(generatedContentDTO)
                .build();
    }


    public ChatSession getNewChatSession(Long userId) {
        ChatSession newSession = new ChatSession();
        newSession.setUserId(userId);
        newSession.setSessionId(UUID.randomUUID().toString());
        newSession.setStatus(ChatStatus.ACTIVE);
        newSession.setCreatedAt(LocalDateTime.now());
        return chatSessionRepository.save(newSession);

    }

    @Transactional
    public void deleteChatSession(String sessionId) {
        chatSessionRepository.findBySessionId(sessionId)
                .ifPresentOrElse(
                        chatSessionRepository::delete,
                        () -> { throw new RuntimeException("Chat session not found for sessionId: " + sessionId); }
                );
    }

    public ResponseEntity<?> updateTitle(String sessionId, String title) {
        Optional<ChatSession> chatSession = chatSessionRepository.findBySessionId(sessionId);
        if (chatSession.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Chat session not found");
        }

        ChatSession session = chatSession.get();
        session.setTitle(title);
        session.setUpdatedAt(LocalDateTime.now());

        chatSessionRepository.save(session);

        return ResponseEntity.ok("Title updated successfully");
    }

    public List<GeneratedImage> getImage(){
        Client client = new Client();

        GenerateImagesConfig config =
                GenerateImagesConfig.builder()
                        .numberOfImages(1)
                        .outputMimeType("image/jpeg")
                        .includeSafetyAttributes(true)
                        .build();

        GenerateImagesResponse response =
                client.models.generateImages(
                        "imagen-3.0-generate-002", "Robot holding a red skateboard", config);

        response.generatedImages().ifPresent(
                images -> {
                    System.out.println("Generated " + images.size() + " images.");
                    Image image = images.get(0).image().orElse(null);
                    // Do something with the image.
                }
        );
        return response.generatedImages().get();
    }
}
