package com.back.postpilot.repository;

import com.back.postpilot.entity.ContentImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentImageRepository extends JpaRepository<ContentImage, Long> {
    
    /**
     * Find images by user ID through the GeneratedContent -> ChatSession relationship
     */
    @Query("SELECT ci FROM ContentImage ci " +
           "JOIN ci.generatedContent gc " +
           "JOIN gc.chatSession cs " +
           "WHERE cs.userId = :userId")
    Page<ContentImage> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find images by session ID through the GeneratedContent -> ChatSession relationship
     */
    @Query("SELECT ci FROM ContentImage ci " +
           "JOIN ci.generatedContent gc " +
           "JOIN gc.chatSession cs " +
           "WHERE cs.sessionId = :sessionId")
    List<ContentImage> findBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * Find images by user ID without pagination
     */
    @Query("SELECT ci FROM ContentImage ci " +
           "JOIN ci.generatedContent gc " +
           "JOIN gc.chatSession cs " +
           "WHERE cs.userId = :userId " +
           "ORDER BY ci.generatedAt DESC")
    List<ContentImage> findByUserIdOrderByGeneratedAtDesc(@Param("userId") Long userId);
    
    /**
     * Search images by tag/keyword in alt text or image prompt (case-insensitive)
     */
    @Query("SELECT ci FROM ContentImage ci WHERE " +
           "LOWER(ci.altText) LIKE LOWER(CONCAT('%', :tag, '%')) OR " +
           "LOWER(ci.imagePrompt) LIKE LOWER(CONCAT('%', :tag, '%'))")
    Page<ContentImage> findByTag(@Param("tag") String tag, Pageable pageable);
    
    /**
     * Search images by tag/keyword in alt text or image prompt (case-insensitive) without pagination
     */
    @Query("SELECT ci FROM ContentImage ci WHERE " +
           "LOWER(ci.altText) LIKE LOWER(CONCAT('%', :tag, '%')) OR " +
           "LOWER(ci.imagePrompt) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<ContentImage> findByTag(@Param("tag") String tag, Sort sort);
}


