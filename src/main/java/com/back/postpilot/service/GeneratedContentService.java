package com.back.postpilot.service;

import com.back.postpilot.DTO.GenerateContentDTO;
import com.back.postpilot.DTO.ScheduledContentDTO;
import com.back.postpilot.entity.GeneratedContent;
import com.back.postpilot.repository.GeneratedContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class GeneratedContentService {

    private final GeneratedContentRepository generatedContentRepository;

    public GeneratedContentService(GeneratedContentRepository generatedContentRepository) {
        this.generatedContentRepository = generatedContentRepository;
    }

    public void testDatabaseConnection(Long userId) {
        log.info("=== DATABASE CONNECTION TEST STARTED ===");
        try {
            // Test 1: Count total records for user
            Long totalCount = generatedContentRepository.countByUserId(userId);
            log.info("TEST 1 - Total records for user {}: {}", userId, totalCount);
            
            // Test 2: Get all records for user (without pagination)
            List<GeneratedContent> allRecords = generatedContentRepository.findAllByUserId(userId);
            log.info("TEST 2 - Retrieved {} records for user {}", allRecords.size(), userId);
            
            if (!allRecords.isEmpty()) {
                log.info("Sample record details:");
                for (int i = 0; i < Math.min(3, allRecords.size()); i++) {
                    GeneratedContent record = allRecords.get(i);
                    log.info("Record {}: ID={}, Title='{}', isScheduled={}, status={}, platform={}, scheduledAt={}, chatSessionId={}", 
                        i + 1,
                        record.getId(),
                        record.getTitle(),
                        record.getIsScheduled(),
                        record.getStatus(),
                        record.getPlatform(),
                        record.getScheduledAt(),
                        record.getChatSession() != null ? record.getChatSession().getId() : "NULL");
                }
            } else {
                log.warn("No records found for user {}", userId);
            }
            
            // Test 3: Test pagination
            Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 5);
            Page<GeneratedContent> pagedRecords = generatedContentRepository.findByChatSessionUserId(userId, pageable);
            log.info("TEST 3 - Paginated query: {} items out of {} total", 
                pagedRecords.getNumberOfElements(), 
                pagedRecords.getTotalElements());
            
            log.info("=== DATABASE CONNECTION TEST COMPLETED SUCCESSFULLY ===");
            
        } catch (Exception e) {
            log.error("=== DATABASE CONNECTION TEST FAILED ===", e);
            throw e;
        }
    }

    public Page<ScheduledContentDTO> getAllScheduledPost(Pageable pageable) {
        log.info("=== SERVICE: Getting ALL content (no user filtering) for frontend categorization ===");
        log.info("Pageable parameters: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            log.info("Calling repository.findAll(pageable) to get ALL content");
            Page<GeneratedContent> rawContent = generatedContentRepository.findAll(pageable);
            
            log.info("Repository call completed successfully");
            log.info("Raw data from DB: {} items out of {} total", 
                rawContent.getNumberOfElements(), 
                rawContent.getTotalElements());
            log.info("Total pages in DB: {}", rawContent.getTotalPages());
            
            if (rawContent.hasContent()) {
                log.info("Raw content found! First item: ID={}, Title='{}', isScheduled={}, status={}, platform={}, scheduledAt={}", 
                    rawContent.getContent().get(0).getId(),
                    rawContent.getContent().get(0).getTitle(),
                    rawContent.getContent().get(0).getIsScheduled(),
                    rawContent.getContent().get(0).getStatus(),
                    rawContent.getContent().get(0).getPlatform(),
                    rawContent.getContent().get(0).getScheduledAt());
            } else {
                log.warn("No raw content found in database");
            }
            
            log.info("Starting DTO conversion...");
            Page<ScheduledContentDTO> result = rawContent.map(content -> {
                log.debug("Converting content: ID={}, Title='{}', isScheduled={}, status={}, platform={}, scheduledAt={}", 
                    content.getId(),
                    content.getTitle(),
                    content.getIsScheduled(),
                    content.getStatus(),
                    content.getPlatform(),
                    content.getScheduledAt());
                
                return new ScheduledContentDTO(
                    content.getId(),
                    content.getTitle(),
                    content.getChatSession() != null ? content.getChatSession().getId() : null,
                    content.getStatus() != null ? content.getStatus().toString() : "DRAFT",
                    content.getIsScheduled() != null ? content.getIsScheduled() : false,
                    content.getScheduledAt(),
                    content.getPlatform() != null ? content.getPlatform().toString() : null
                );
            });
            
            log.info("DTO conversion completed successfully");
            log.info("Final result: {} DTOs out of {} total", 
                result.getNumberOfElements(), 
                result.getTotalElements());
            log.info("=== SERVICE: Successfully retrieved ALL content for frontend categorization ===");
            
            return result;
            
        } catch (Exception e) {
            log.error("Error in getAllScheduledPost", e);
            throw e;
        }
    }

    public Page<ScheduledContentDTO> getAllScheduledPostByUser(Long userId, Pageable pageable) {
        log.info("=== SERVICE: Getting scheduled posts for user: {} ===", userId);
        log.info("Pageable parameters: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        // First, test database connection and data retrieval
        testDatabaseConnection(userId);
        
        try {
            log.info("Calling repository.findByChatSessionUserId(userId={}, pageable)", userId);
            Page<GeneratedContent> rawContent = generatedContentRepository.findByChatSessionUserId(userId, pageable);
            
            log.info("Repository call completed successfully");
            log.info("Raw data from DB: {} items out of {} total", 
                rawContent.getNumberOfElements(), 
                rawContent.getTotalElements());
            log.info("Total pages in DB: {}", rawContent.getTotalPages());
            
            if (rawContent.hasContent()) {
                log.info("Raw content found! Processing {} items", rawContent.getNumberOfElements());
                for (int i = 0; i < rawContent.getContent().size(); i++) {
                    GeneratedContent content = rawContent.getContent().get(i);
                    log.info("Raw item {}: ID={}, Title='{}', isScheduled={}, status={}, platform={}, scheduledAt={}", 
                        i + 1, 
                        content.getId(), 
                        content.getTitle(),
                        content.getIsScheduled(),
                        content.getStatus(),
                        content.getPlatform(),
                        content.getScheduledAt());
                }
            } else {
                log.warn("No raw content found in database for user: {}", userId);
            }
            
            log.info("Starting DTO conversion...");
            Page<ScheduledContentDTO> result = rawContent.map(content -> {
                log.debug("Converting content ID: {} to DTO", content.getId());
                ScheduledContentDTO dto = new ScheduledContentDTO(
                    content.getId(),
                    content.getTitle(),
                    content.getChatSession() != null ? content.getChatSession().getId() : null,
                    content.getStatus() != null ? content.getStatus().toString() : "DRAFT",
                    content.getIsScheduled() != null ? content.getIsScheduled() : false,
                    content.getScheduledAt(),
                    content.getPlatform() != null ? content.getPlatform().toString() : null
                );
                log.debug("Created DTO: {}", dto);
                return dto;
            });
            
            log.info("DTO conversion completed successfully");
            log.info("Final result: {} DTOs out of {} total", 
                result.getNumberOfElements(), 
                result.getTotalElements());
            
            if (result.hasContent()) {
                log.info("Final DTOs:");
                for (int i = 0; i < result.getContent().size(); i++) {
                    log.info("DTO {}: {}", i + 1, result.getContent().get(i));
                }
            }
            
            log.info("=== SERVICE: Successfully retrieved scheduled posts for user: {} ===", userId);
            return result;
            
        } catch (Exception e) {
            log.error("Error in getAllScheduledPostByUser for userId: {}", userId, e);
            throw e;
        }
    }

    public ResponseEntity<?> updateContent(GenerateContentDTO generateContentDTO) {
        GeneratedContent content = generatedContentRepository.getReferenceById(generateContentDTO.id());
        content.setContent(generateContentDTO.content());
        GeneratedContent updated = generatedContentRepository.save(content);
        return ResponseEntity.ok(updated);
    }

    public String getGeneratedContentById(Long id) {
        return generatedContentRepository.findById(id)
                .map(GeneratedContent::getContent)
                .orElse(null);
    }

    public boolean updateGeneratedContentById(Long id, String newContent) {
        return generatedContentRepository.findById(id).map(entity -> {
            entity.setContent(newContent);
            generatedContentRepository.save(entity);
            return true;
        }).orElse(false);
    }
}
