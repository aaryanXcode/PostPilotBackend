package com.back.postpilot.DTO;


import com.back.postpilot.EnumTypeConstants.ContentStatus;
import com.back.postpilot.EnumTypeConstants.ContentType;
import com.back.postpilot.entity.ContentImage;
import jdk.jshell.Snippet;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GenerateContentDTO(
        Long id,
        String title,
        String content,
        String hashtags,
        String platform,
        LocalDateTime createdAt,
        ContentType contentType,
        ContentStatus status,
        List<ContentImage> imageUrls,
        String metadata
) {
}
