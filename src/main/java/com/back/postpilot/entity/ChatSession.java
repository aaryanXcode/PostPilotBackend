package com.back.postpilot.entity;

import com.back.postpilot.EnumTypeConstants.ChatStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "p_chat_session")
@ToString(exclude = {"messages", "generatedContents"})
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true)
    private String sessionId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title")
    private String title;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ChatStatus status; // ACTIVE, ARCHIVED, DELETED

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL)
    private List<GeneratedContent> generatedContents = new ArrayList<>();


}
