package com.back.postpilot.repository;

import com.back.postpilot.entity.GeneratedContent;
import com.back.postpilot.EnumTypeConstants.ContentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<GeneratedContent, Long> {

    // Count total published posts by user
    @Query("SELECT COUNT(gc) FROM GeneratedContent gc " +
           "JOIN gc.chatSession cs " +
           "WHERE cs.userId = :userId " +
           "AND gc.status = 'PUBLISHED'")
    Long countTotalPostsByUser(@Param("userId") Long userId);

    // Count total published posts by user with date range
    @Query("SELECT COUNT(gc) FROM GeneratedContent gc " +
           "JOIN gc.chatSession cs " +
           "WHERE cs.userId = :userId " +
           "AND gc.status = 'PUBLISHED' " +
           "AND gc.createdAt BETWEEN :startDate AND :endDate")
    Long countTotalPostsByUserAndDateRange(@Param("userId") Long userId, 
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    // Get recent posts by user with performance data
    @Query("SELECT gc FROM GeneratedContent gc " +
           "JOIN gc.chatSession cs " +
           "WHERE cs.userId = :userId " +
           "ORDER BY gc.createdAt DESC")
    List<GeneratedContent> findRecentPostsByUser(@Param("userId") Long userId, Pageable pageable);

    // Get posts by status for user
    @Query("SELECT gc FROM GeneratedContent gc " +
           "JOIN gc.chatSession cs " +
           "WHERE cs.userId = :userId " +
           "AND gc.status = :status")
    List<GeneratedContent> findPostsByUserAndStatus(@Param("userId") Long userId, 
                                                   @Param("status") ContentStatus status);

    // Get best performing post by engagement (mock data for now)
    @Query("SELECT gc FROM GeneratedContent gc " +
           "JOIN gc.chatSession cs " +
           "WHERE cs.userId = :userId " +
           "AND gc.status = 'PUBLISHED' " +
           "ORDER BY gc.createdAt DESC")
    List<GeneratedContent> findBestPerformingPosts(@Param("userId") Long userId, Pageable pageable);

    // Get posts by date range
    @Query("SELECT gc FROM GeneratedContent gc " +
           "JOIN gc.chatSession cs " +
           "WHERE cs.userId = :userId " +
           "AND gc.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY gc.createdAt DESC")
    List<GeneratedContent> findPostsByUserAndDateRange(@Param("userId") Long userId,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    // Get published posts count (simplified - count all published posts)
    @Query("SELECT COUNT(gc) FROM GeneratedContent gc WHERE gc.status = 'PUBLISHED'")
    Long countPublishedPostsByUser();

    // Get scheduled posts count (simplified - count all scheduled posts)
    @Query("SELECT COUNT(gc) FROM GeneratedContent gc WHERE gc.scheduledAt IS NOT NULL")
    Long countScheduledPostsByUser();

    // Get draft posts count (simplified - count all draft posts)
    @Query("SELECT COUNT(gc) FROM GeneratedContent gc WHERE gc.status = 'DRAFT'")
    Long countDraftPostsByUser();

    // Count all generated posts by user (all rows in p_generated_content for this user)
    @Query("SELECT COUNT(gc) FROM GeneratedContent gc " +
           "JOIN gc.chatSession cs " +
           "WHERE cs.userId = :userId")
    Long countAllGeneratedPostsByUser(@Param("userId") Long userId);

    // Count all generated posts by user with date range
    @Query("SELECT COUNT(gc) FROM GeneratedContent gc " +
           "JOIN gc.chatSession cs " +
           "WHERE cs.userId = :userId " +
           "AND gc.createdAt BETWEEN :startDate AND :endDate")
    Long countAllGeneratedPostsByUserAndDateRange(@Param("userId") Long userId, 
                                                 @Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);

    // Find all chat sessions for a user (for debugging)
    @Query("SELECT cs FROM ChatSession cs WHERE cs.userId = :userId")
    List<com.back.postpilot.entity.ChatSession> findAllChatSessionsByUser(@Param("userId") Long userId);

    // Count all generated content records (for debugging)
    @Query("SELECT COUNT(gc) FROM GeneratedContent gc")
    Long countAllGeneratedContent();

    // Find all generated content (for debugging)
    @Query("SELECT gc FROM GeneratedContent gc")
    List<GeneratedContent> findAllGeneratedContent();

    // Get all unique status values for debugging
    @Query("SELECT DISTINCT gc.status FROM GeneratedContent gc")
    List<String> findAllDistinctStatuses();

    // Get all unique scheduledAt values for debugging
    @Query("SELECT DISTINCT gc.scheduledAt FROM GeneratedContent gc WHERE gc.scheduledAt IS NOT NULL")
    List<LocalDateTime> findAllDistinctScheduledAt();

    // Get all user IDs that have generated content for debugging
    @Query("SELECT DISTINCT cs.userId FROM GeneratedContent gc JOIN gc.chatSession cs")
    List<Long> findAllDistinctUserIds();

    // Get all generated content with user info for debugging
    @Query("SELECT gc.id, gc.status, gc.scheduledAt, cs.userId FROM GeneratedContent gc JOIN gc.chatSession cs")
    List<Object[]> findAllGeneratedContentWithUserInfo();
}
