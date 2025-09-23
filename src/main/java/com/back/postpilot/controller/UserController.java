package com.back.postpilot.controller;

import com.back.postpilot.DTO.ChatHistoryDTO;
import com.back.postpilot.DTO.UserProfileDTO;
import com.back.postpilot.entity.Role;
import com.back.postpilot.entity.UserEntity;
import com.back.postpilot.service.ChatHistoryService;
import com.back.postpilot.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        try {
            UserEntity user = userDetailsService.findByUsername(userDetails.getUsername());
            UserProfileDTO dto = new UserProfileDTO(user.getId(), user.getUsername(), user.getRole(), user.getEmail());
            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            log.debug(ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("authentication revoked!");
        }
    }

    @GetMapping("/chat_history")
    ResponseEntity<List<ChatHistoryDTO>> getChatHistory(@AuthenticationPrincipal UserDetails userDetails,@RequestParam(required = false) Long userId){
        log.debug("fetching chat history based on user and their roles");
        UserEntity user = userDetailsService.findByUsername(userDetails.getUsername());
        Role role = user.getRole();
        if (userId != null && role == Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long targetUserId = (userId != null && (role == Role.ADMIN || role == Role.SUPER_ADMIN))
                ? userId
                : user.getId();
        List<ChatHistoryDTO> chatHistoryDTOList = chatHistoryService.getChatHistoryByUserAndRole(targetUserId, role);
        return ResponseEntity.ok(chatHistoryDTOList);
    }




}
