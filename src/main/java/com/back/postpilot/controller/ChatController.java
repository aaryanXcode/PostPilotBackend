package com.back.postpilot.controller;


import com.back.postpilot.DTO.ChatMessageDTO;
import com.back.postpilot.DTO.PageRequestDTO;
import com.back.postpilot.DTO.UserProfileDTO;
import com.back.postpilot.domain.ContentGenerationRequest;
import com.back.postpilot.entity.ChatSession;
import com.back.postpilot.entity.UserEntity;
import com.back.postpilot.service.ChatHistoryService;
import com.back.postpilot.service.ChatService;
import com.back.postpilot.service.CustomUserDetailsService;
import com.google.genai.types.GeneratedImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import com.back.postpilot.EnumTypeConstants.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    ChatService chatService;

    @Autowired
    ChatHistoryService chatHistoryService;

    @Autowired
    CustomUserDetailsService userDetailsService;

    @PostMapping("/content")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('USER')")
    ResponseEntity<?> getResponse(String prompt){
        try{
            return ResponseEntity.ok("done");
        }
        catch(Exception ex) {
            return ResponseEntity.badRequest().body("bad request");
        }
    }


    @PostMapping("/assistant")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('USER')")
    ResponseEntity<?> getResponseGemini(@RequestBody ContentGenerationRequest request){
        log.info("=== CHAT ASSISTANT API CALL STARTED ===");
        log.info("Request received: {}", request);
        try{
            ChatMessageDTO response = chatService.getResponse(request);
            log.info("Chat service response generated successfully");
            log.info("=== CHAT ASSISTANT API CALL COMPLETED SUCCESSFULLY ===");
            return ResponseEntity.ok(response);
        } catch(Exception ex){
            log.error("Error in chat assistant endpoint", ex);
            return ResponseEntity.badRequest().body("bad Request");
        }
    }

    @PostMapping("/{sessionId}/messages")
    ResponseEntity<Page<ChatMessageDTO>> getChatHistoryBySession(@PathVariable String sessionId, @RequestBody PageRequestDTO pageRequestDTO, @AuthenticationPrincipal UserDetails userDetails){
        Pageable pageable = PageRequest.of(pageRequestDTO.page(), pageRequestDTO.size(), Sort.by("timestamp").descending());
        Page<ChatMessageDTO> messages = chatHistoryService.getChatMessageHistoryBySessionId(sessionId, userDetails, pageable);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/create/session")
    ResponseEntity<String> getNewChatSession(@AuthenticationPrincipal UserDetails userDetails){
        log.debug("creating new chat session");
        UserEntity user = userDetailsService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(chatService.getNewChatSession(user.getId()).getSessionId());
    }

    @DeleteMapping("/delete/session/{sessionId}")
    public ResponseEntity<String> deleteChatSession(@PathVariable String sessionId) {
        chatService.deleteChatSession(sessionId);
        return ResponseEntity.ok("Chat session deleted successfully");
    }

    @PutMapping("/update/title/{sessionId}")
    public ResponseEntity<?> updateChatTitle(
            @PathVariable String sessionId,
            @RequestParam String title) {

       return chatService.updateTitle(sessionId, title);
    }

    @GetMapping("/image")
    List<GeneratedImage> getGeneratedImage(){
        return chatService.getImage();
    }

    @GetMapping("/models")
    List<AssitanceModels> getAllModels(){
        return Arrays.stream(AssitanceModels.values()).toList();
    }

    @GetMapping("/platforms")
    List<ContentPlatForms> getAllContentPlatforms(){
        return Arrays.stream(ContentPlatForms.values()).toList();
    }



}

