package com.back.postpilot.DTO;

import com.back.postpilot.EnumTypeConstants.AssitanceModels;
import com.back.postpilot.EnumTypeConstants.MessageType;
import com.back.postpilot.entity.ChatSession;
import lombok.Data;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;

import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    private Long id;
    private String title;
    private String sessionId;
    private String sender;
    private String content;
    private MessageType messageType; // USER, ASSISTANT, SYSTEM
    private AssitanceModels modelType;
    private LocalDateTime timestamp;
    private Integer sequenceNumber;
    private GenerateContentDTO generateContentDTO;

    public ChatMessageDTO(){}
    public ChatMessageDTO(
            Long id,
            String sessionId,
            String content,
            MessageType messageType,
            LocalDateTime timestamp
    ) {
        this.id = id;
        this.sessionId = sessionId;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = timestamp;
    }

    public ChatMessageDTO(
            Long id,
            String sessionId,
            String content,
            MessageType messageType,
            LocalDateTime timestamp,
            GenerateContentDTO generatedContentDTO
    ) {
        this.id = id;
        this.sessionId = sessionId;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.generateContentDTO = generatedContentDTO;
    }



    public static class Builder {
        private final ChatMessageDTO dto = new ChatMessageDTO();

        public Builder id(Long id) { dto.id = id; return this; }
        public Builder title(String title) { dto.title = title; return this; }
        public Builder sessionId(String sessionId) { dto.sessionId = sessionId; return this; }
        public Builder sender(String sender) { dto.sender = sender; return this; }
        public Builder content(String content) { dto.content = content; return this; }
        public Builder messageType(MessageType type) { dto.messageType = type; return this; }
        public Builder timestamp(LocalDateTime time) { dto.timestamp = time; return this; }
        public Builder sequenceNumber(Integer seq) { dto.sequenceNumber = seq; return this; }
        public Builder modelType(AssitanceModels modelType) { dto.modelType = modelType; return this; }
        public Builder generateContentDTO(GenerateContentDTO generateContentDTO) { dto.generateContentDTO = generateContentDTO; return this; }
        public ChatMessageDTO build() { return dto; }
    }
}

