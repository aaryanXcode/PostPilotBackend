package com.back.postpilot.DTO;

import com.back.postpilot.EnumTypeConstants.ChatStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChatSessionDTO {
    private Long id;
    private String sessionId;
    private String userId;
    private String title;
    private ChatStatus status; // ACTIVE, ARCHIVED, DELETED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ChatMessageDTO> messages;

    public ChatSessionDTO() {
        this.messages = new ArrayList<>();
    }

    public static class Builder {
        private final ChatSessionDTO dto = new ChatSessionDTO();

        public Builder id(Long id) { dto.id = id; return this; }
        public Builder sessionId(String sessionId) { dto.sessionId = sessionId; return this; }
        public Builder userId(String userId) { dto.userId = userId; return this; }
        public Builder title(String title) { dto.title = title; return this; }
        public Builder status(ChatStatus status) { dto.status = status; return this; }
        public Builder createdAt(LocalDateTime dateTime) { dto.createdAt = dateTime; return this; }
        public Builder updatedAt(LocalDateTime dateTime) { dto.updatedAt = dateTime; return this; }
        public Builder messages(List<ChatMessageDTO> messages) { dto.messages = messages; return this; }
        public ChatSessionDTO build() { return dto; }
    }

}
