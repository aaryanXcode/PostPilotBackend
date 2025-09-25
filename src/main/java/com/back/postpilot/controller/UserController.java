package com.back.postpilot.controller;

import com.back.postpilot.DTO.CreateUserRequestDTO;
import com.back.postpilot.DTO.UserResponseDTO;
import com.back.postpilot.service.UserService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        try {
            log.info("=== CREATE USER API CALL STARTED ===");
            log.info("Creating user with username: {}", request.username());
            log.info("User email: {}", request.email());
            log.info("User role: {}", request.role());
            
            UserResponseDTO createdUser = userService.createUser(request);
            
            log.info("User created successfully with ID: {}", createdUser.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
            
        } catch (IllegalArgumentException e) {
            log.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        try {
            log.info("=== GET ALL USERS API CALL STARTED ===");
            List<UserResponseDTO> users = userService.getAllUsers();
            log.info("Retrieved {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            log.info("=== GET USER BY ID API CALL STARTED ===");
            log.info("Fetching user with ID: {}", id);
            
            UserResponseDTO user = userService.getUserById(id);
            log.info("User found: {}", user.username());
            return ResponseEntity.ok(user);
            
        } catch (IllegalArgumentException e) {
            log.error("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error fetching user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred");
        }
    }
}