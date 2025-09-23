package com.back.postpilot.service;

import com.back.postpilot.DTO.GenerateContentDTO;
import com.back.postpilot.DTO.ScheduledContentDTO;
import com.back.postpilot.entity.GeneratedContent;
import com.back.postpilot.repository.GeneratedContentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GeneratedContentService {

    private final GeneratedContentRepository generatedContentRepository;

    public GeneratedContentService(GeneratedContentRepository generatedContentRepository) {
        this.generatedContentRepository = generatedContentRepository;
    }

    public Page<ScheduledContentDTO> getAllScheduledPost(Pageable pageable) {
        return generatedContentRepository.findAll(pageable)
                .map(content -> new ScheduledContentDTO(
                        content.getId(),
                        content.getTitle(),
                        content.getChatSession() != null ? content.getChatSession().getId() : null,
                        content.getStatus().toString(),
                        content.getIsScheduled(),
                        content.getScheduledAt()
                ));
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
