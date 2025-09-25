package com.back.postpilot.DTO;

import java.time.LocalDateTime;

public record ScheduledContentDTO(
        Long id,
        String title,
        Long chatId,
        String contentStatus,
        boolean isScheduled,
        LocalDateTime scheduledDate,
        String platform
) {}
