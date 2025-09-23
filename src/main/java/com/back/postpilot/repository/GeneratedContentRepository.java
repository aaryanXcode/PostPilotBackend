package com.back.postpilot.repository;

import com.back.postpilot.DTO.ChatMessageDTO;
import com.back.postpilot.entity.ChatSession;
import com.back.postpilot.entity.GeneratedContent;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GeneratedContentRepository extends JpaRepository<GeneratedContent, Long> {
    List<GeneratedContent> findByChatMessageIdIn(List<Long> chatMessageId);

    @Modifying
    @Transactional
    @Query("UPDATE GeneratedContent g " +
            "SET g.isScheduled = true, g.scheduledAt = :scheduledAt " +
            "WHERE g.id = :id")
    int updateSchedule(@Param("id") Long id, @Param("scheduledAt") LocalDateTime scheduleTime);

    @Modifying
    @Transactional
    @Query("UPDATE GeneratedContent g " +
            "SET g.isScheduled = false " +
            "WHERE g.id = :id")
    int updateScheduleFalse(@Param("id") Long id);

    List<GeneratedContent> findByIsScheduledTrue();

    Page<GeneratedContent> findByIsScheduledTrue(Pageable pageable);

}
