package com.back.postpilot.controller;

import com.back.postpilot.DTO.GenerateContentDTO;
import com.back.postpilot.DTO.ScheduledContentDTO;
import com.back.postpilot.service.GeneratedContentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/generated-content")
public class GeneratedContentController {

    private final GeneratedContentService generatedContentService;

    public GeneratedContentController(GeneratedContentService generatedContentService) {
        this.generatedContentService = generatedContentService;
    }

    @GetMapping("/scheduled")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<ScheduledContentDTO>> getScheduledContent(
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        Page<ScheduledContentDTO> scheduledContentDTOS = generatedContentService.getAllScheduledPost(pageable);
        return ResponseEntity.ok(scheduledContentDTOS);
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


