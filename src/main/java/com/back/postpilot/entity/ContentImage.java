package com.back.postpilot.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "p_content_image")
public class ContentImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_content_id")
    private GeneratedContent generatedContent;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_prompt")
    private String imagePrompt;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private String fileSize;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
}
