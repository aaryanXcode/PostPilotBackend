package com.back.postpilot.service;

import com.back.postpilot.DTO.ChatHistoryDTO;
import com.back.postpilot.DTO.ChatMessageDTO;
import com.back.postpilot.DTO.GenerateContentDTO;
import com.back.postpilot.entity.ChatSession;
import com.back.postpilot.entity.GeneratedContent;
import com.back.postpilot.entity.Role;
import com.back.postpilot.entity.UserEntity;
import com.back.postpilot.repository.ChatMessageRepository;
import com.back.postpilot.repository.ChatSessionRepository;
import com.back.postpilot.repository.GeneratedContentRepository;
import com.back.postpilot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatHistoryService {

    @Autowired
    ChatSessionRepository chatSessionRepository;

    @Autowired
    UserRepository userEntityRepository;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    GeneratedContentRepository generatedContentRepository;

    List<ChatHistoryDTO> getAllChatHistory(Long userId){
        return chatSessionRepository.findByUserId(userId);
    }

    public List<ChatHistoryDTO> getChatHistoryByUserAndRole(Long userId, Role role) {
        if(role.equals(Role.USER)){
            return chatSessionRepository.findByUserId(userId);
        }
        else if(role.equals(Role.ADMIN)){
            List<Long> userIds = userEntityRepository.findAllByRole(Role.USER)
                    .stream()
                    .map(UserEntity::getId)
                    .collect(Collectors.toCollection(ArrayList::new));
            userIds.add(userId);
            return chatSessionRepository.findByUserIdIn(userIds);
        }
        return chatSessionRepository.findAllUsersChatHistory();
    }

    public Page<ChatMessageDTO> getChatMessageHistoryBySessionId(String sessionId, UserDetails userDetails, Pageable pageable) {
        Long chatSessionId = chatSessionRepository.findIdBySessionId(sessionId);
        Page<ChatMessageDTO> chatmessagePage = chatMessageRepository.findByChatSessionOrderBySequenceNumberDesc(chatSessionId, pageable);
        List<Long> chatIdList = chatmessagePage.stream().map(ChatMessageDTO::getId).toList();
        List<GeneratedContent> generatedContents = generatedContentRepository.findByChatMessageIdIn(chatIdList);
        Map<Long, GenerateContentDTO> generatedContentMap = generatedContents.stream()
                .collect(Collectors.toMap(
                        gc -> gc.getChatMessage().getId(),
                        gc -> new GenerateContentDTO(
                                gc.getId(),
                                gc.getTitle(),
                                gc.getContent(),
                                gc.getHashtags(),
                                gc.getPlatform().getPlatform(),
                                gc.getCreatedAt(),
                                gc.getContentType(),
                                gc.getStatus(),
                                gc.getImages(),
                                gc.getMetadata() != null ? gc.getMetadata() : null
                        )
                ));

        List<ChatMessageDTO> updatedContent = chatmessagePage.stream()
                .peek(dto -> {
                    dto.setGenerateContentDTO(generatedContentMap.get(dto.getId())); // set nested DTO
                })
                .toList();
        return new PageImpl<>(
                updatedContent,
                pageable,
                chatmessagePage.getTotalElements()
        );
    }
}
