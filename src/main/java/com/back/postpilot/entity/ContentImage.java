package com.back.postpilot.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "p_content_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_content_id")
    @JsonIgnore
    private GeneratedContent generatedContent;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "image_prompt", columnDefinition = "TEXT")
    private String imagePrompt;

    @Column(name = "alt_text", length = 512)
    private String altText;

    @Column(name = "file_name", length = 512)
    private String fileName;

    @Column(name = "file_size", length = 64)
    private String fileSize;

    @Column(name = "image_data", columnDefinition = "TEXT")
    private String imageData;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
}
