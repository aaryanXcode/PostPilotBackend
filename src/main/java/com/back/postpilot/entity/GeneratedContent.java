package com.back.postpilot.entity;

import com.back.postpilot.EnumTypeConstants.ContentPlatForms;
import com.back.postpilot.EnumTypeConstants.ContentStatus;
import com.back.postpilot.EnumTypeConstants.ContentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "p_generated_content")
public class GeneratedContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "chat_message_id")
    private ChatMessage chatMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_session_id")
    private ChatSession chatSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform")
    private ContentPlatForms platform;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type")
    private ContentType contentType;

    @Column(name = "title")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "hashtags")
    private String hashtags;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "scheduledAt")
    private LocalDateTime scheduledAt;

    @NotNull
    @Column(name = "is_scheduled", nullable = false)
    @Builder.Default
    private Boolean isScheduled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ContentStatus status = ContentStatus.DRAFT;

    @OneToMany(mappedBy = "generatedContent", cascade = CascadeType.ALL)
    private List<ContentImage> images = new ArrayList<>();

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}
