package com.back.postpilot.repository;

import com.back.postpilot.DTO.ChatHistoryDTO;
import com.back.postpilot.entity.ChatSession;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    @NotNull
    @Override
    Optional<ChatSession> findById(Long Id);

    Optional<ChatSession> findBySessionId(String sessionId);



    @Query("SELECT new com.back.postpilot.DTO.ChatHistoryDTO(c.title, c.sessionId, c.userId) " +
            "FROM ChatSession c WHERE c.userId = :userId")
    List<ChatHistoryDTO> findByUserId(@Param("userId") Long userId);

    @Query("SELECT new com.back.postpilot.DTO.ChatHistoryDTO(c.title, c.sessionId, c.userId) " +
            "FROM ChatSession c WHERE c.userId IN :userIds")
    List<ChatHistoryDTO> findByUserIdIn(@Param("userIds") List<Long> userIds);

    @Query("SELECT new com.back.postpilot.DTO.ChatHistoryDTO(c.title, c.sessionId, c.userId) " +
            "FROM ChatSession c")
    List<ChatHistoryDTO> findAllUsersChatHistory();

    @Query("SELECT c.id FROM ChatSession c WHERE c.sessionId = :sessionId")
    Long findIdBySessionId(@Param("sessionId") String sessionId);
}
