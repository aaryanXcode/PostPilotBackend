package com.back.postpilot.controller;

import com.back.postpilot.DTO.GenerateContentDTO;
import com.back.postpilot.DTO.ScheduledContentDTO;
import com.back.postpilot.service.GeneratedContentService;
import com.back.postpilot.service.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/generated-content")
public class GeneratedContentController {

    private final GeneratedContentService generatedContentService;
    private final CustomUserDetailsService customUserDetailsService;

    public GeneratedContentController(GeneratedContentService generatedContentService, CustomUserDetailsService customUserDetailsService) {
        this.generatedContentService = generatedContentService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @GetMapping("/scheduled")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('SUPER_ADMIN') OR hasRole('USER')")
    public ResponseEntity<Page<ScheduledContentDTO>> getScheduledContent(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("=== SCHEDULED CONTENT API CALL STARTED ===");
        log.info("Request received for scheduled content");
        log.info("UserDetails: {}", userDetails);
        log.info("Pageable: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            log.info("Calling service to get ALL scheduled posts (no user filtering)");
            Page<ScheduledContentDTO> scheduledContentDTOS = generatedContentService.getAllScheduledPost(pageable);
            
            log.info("Service call completed successfully");
            log.info("Retrieved {} items out of {} total", 
                scheduledContentDTOS.getNumberOfElements(), 
                scheduledContentDTOS.getTotalElements());
            log.info("Total pages: {}", scheduledContentDTOS.getTotalPages());
            log.info("Current page: {}", scheduledContentDTOS.getNumber());
            
            if (scheduledContentDTOS.hasContent()) {
                log.info("Content found! First item: {}", scheduledContentDTOS.getContent().get(0));
            } else {
                log.warn("No content found in database");
            }
            
            log.info("=== SCHEDULED CONTENT API CALL COMPLETED SUCCESSFULLY ===");
            return ResponseEntity.ok(scheduledContentDTOS);
            
        } catch (Exception e) {
            log.error("Error in getScheduledContent endpoint", e);
            throw e;
        }
    }

    @GetMapping("/test-db/{userId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('SUPER_ADMIN') OR hasRole('USER')")
    public ResponseEntity<String> testDatabaseConnection(@PathVariable Long userId) {
        log.info("=== TEST DATABASE ENDPOINT CALLED ===");
        log.info("Testing database connection for userId: {}", userId);
        
        try {
            generatedContentService.testDatabaseConnection(userId);
            return ResponseEntity.ok("Database connection test completed successfully. Check logs for details.");
        } catch (Exception e) {
            log.error("Database connection test failed", e);
            return ResponseEntity.status(500).body("Database connection test failed: " + e.getMessage());
        }
    }

    @PostMapping("/update/content")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateContent(@RequestBody GenerateContentDTO generateContentDTO){
        return generatedContentService.updateContent(generateContentDTO);
    }
    @PostMapping("/demo-post")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> demoPost(@RequestBody Map<String, String> body) {
        System.out.println("Received body: " + body);
        return ResponseEntity.ok("Demo POST successful");
    }

    @GetMapping("/post/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> getGeneratedContentById(@PathVariable Long id) {
        String content = generatedContentService.getGeneratedContentById(id);
        if (content != null) {
            return ResponseEntity.ok(content);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/post/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> updateGeneratedContent(@PathVariable Long id, @RequestBody String newContent) {
        boolean updated = generatedContentService.updateGeneratedContentById(id, newContent);
        if (updated) {
            return ResponseEntity.ok("Content updated successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}


