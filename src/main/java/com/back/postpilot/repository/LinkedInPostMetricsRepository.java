package com.back.postpilot.repository;

import com.back.postpilot.entity.LinkedInPostMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LinkedInPostMetricsRepository extends JpaRepository<LinkedInPostMetrics, Long> {
    
    /**
     * Find metrics by post ID
     */
    Optional<LinkedInPostMetrics> findByPostId(String postId);
    
    /**
     * Find all metrics for a specific author
     */
    List<LinkedInPostMetrics> findByAuthorIdOrderByPostedAtDesc(String authorId);
    
    /**
     * Find metrics for author with pagination
     */
    Page<LinkedInPostMetrics> findByAuthorIdOrderByPostedAtDesc(String authorId, Pageable pageable);
    
    /**
     * Find metrics within date range
     */
    List<LinkedInPostMetrics> findByAuthorIdAndPostedAtBetweenOrderByPostedAtDesc(
            String authorId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find top performing posts by engagement rate
     */
    @Query("SELECT l FROM LinkedInPostMetrics l WHERE l.authorId = :authorId " +
           "ORDER BY l.engagementRate DESC")
    List<LinkedInPostMetrics> findTopPerformingPostsByEngagement(@Param("authorId") String authorId, Pageable pageable);
    
    /**
     * Find posts with highest impressions
     */
    @Query("SELECT l FROM LinkedInPostMetrics l WHERE l.authorId = :authorId " +
           "ORDER BY l.impressions DESC")
    List<LinkedInPostMetrics> findTopPostsByImpressions(@Param("authorId") String authorId, Pageable pageable);
    
    /**
     * Get average engagement rate for author
     */
    @Query("SELECT AVG(l.engagementRate) FROM LinkedInPostMetrics l WHERE l.authorId = :authorId")
    Double getAverageEngagementRate(@Param("authorId") String authorId);
    
    /**
     * Get total metrics for author
     */
    @Query("SELECT SUM(l.impressions), SUM(l.clicks), SUM(l.likes), SUM(l.comments), SUM(l.shares) " +
           "FROM LinkedInPostMetrics l WHERE l.authorId = :authorId")
    Object[] getTotalMetricsForAuthor(@Param("authorId") String authorId);
    
    /**
     * Find posts by content type
     */
    List<LinkedInPostMetrics> findByAuthorIdAndContentTypeOrderByPostedAtDesc(
            String authorId, String contentType);
    
    /**
     * Find posts with media
     */
    List<LinkedInPostMetrics> findByAuthorIdAndHasMediaTrueOrderByPostedAtDesc(String authorId);
    
    /**
     * Get metrics summary for date range
     */
    @Query("SELECT COUNT(l), AVG(l.engagementRate), SUM(l.impressions), SUM(l.clicks), " +
           "SUM(l.likes), SUM(l.comments), SUM(l.shares) " +
           "FROM LinkedInPostMetrics l WHERE l.authorId = :authorId " +
           "AND l.postedAt BETWEEN :startDate AND :endDate")
    Object[] getMetricsSummaryForDateRange(@Param("authorId") String authorId, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
}
