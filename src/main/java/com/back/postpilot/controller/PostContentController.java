package com.back.postpilot.controller;

import com.back.postpilot.DTO.GenerateContentDTO;
import com.back.postpilot.DTO.ScheduleRequestDTO;
import com.back.postpilot.entity.GeneratedContent;
import com.back.postpilot.service.PostContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/content")
public class PostContentController {

    private final PostContentService postContentService;
    PostContentController(PostContentService postContentService){
        this.postContentService = postContentService;
    }
    @PostMapping("/post")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> postContent(@RequestBody GenerateContentDTO generateContentDTO) {
        try {
            log.debug("Received content: {}", generateContentDTO.toString());

            // Call the posting service and get the response
            String postingResponse = postContentService.postContent(generateContentDTO); // Fixed method name

            // Create success response
            Map<String, Object> successResponse = Map.of(
                    "success", true,
                    "message", "Content posted successfully",
                    "platformResponse", postingResponse // Include the actual platform response
            );

            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            log.error("Error posting content", e);

            // Create error response
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "error", "Failed to post content",
                    "message", e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }


    @PostMapping("/post/update")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('SUPER_ADMIN)")
    ResponseEntity<?> updateGeneratedContent(@RequestBody GenerateContentDTO generateContentDTO){
        log.debug(generateContentDTO.toString());
        return ResponseEntity.ok("post updated");
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> schedulePost(@RequestBody ScheduleRequestDTO request) {
        boolean isScheduled = postContentService.setSchedulePost(request);
        if (isScheduled) {
            return ResponseEntity.ok(
                    "Post " + request.id() + " scheduled at " + request.dateTime()
            );
        } else {
            return ResponseEntity.status(404).body(
                    "No post found with id " + request.id()
            );
        }
    }

    @PostMapping("/demo")
    String demoPostContent(){

        return "ok";
    }

}
