package com.back.postpilot.entity;

import com.back.postpilot.EnumTypeConstants.MessageType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "p_chat_message")
@ToString(exclude = "chatSession")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_session_id")
    @JsonIgnore
    private ChatSession chatSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;


    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    @OneToOne(mappedBy = "chatMessage", cascade = CascadeType.ALL)
    private GeneratedContent generatedContent;


}
