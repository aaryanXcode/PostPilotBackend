package com.back.postpilot.repository;

import com.back.postpilot.DTO.ChatMessageDTO;
import com.back.postpilot.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long > {
    @Query(" SELECT new com.back.postpilot.DTO.ChatMessageDTO(m.id, m.chatSession.sessionId, m.content, m.messageType, m.timestamp) " +
        "FROM ChatMessage m WHERE m.chatSession.id = :chatSessionId ORDER BY m.timestamp DESC ")
    Page<ChatMessageDTO> findByChatSessionOrderBySequenceNumberDesc(@Param("chatSessionId") Long chatSessionId, Pageable pageable);
}


