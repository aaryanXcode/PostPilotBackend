package com.back.postpilot.controller;

import com.back.postpilot.DTO.CreateUserRequestDTO;
import com.back.postpilot.DTO.UpdateUserRequestDTO;
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequestDTO request) {
        try {
            log.info("=== UPDATE USER API CALL STARTED ===");
            log.info("Updating user with ID: {}", id);
            log.info("Update request: {}", request);
            
            UserResponseDTO updatedUser = userService.updateUser(id, request);
            
            log.info("User updated successfully with ID: {}", updatedUser.id());
            return ResponseEntity.ok(updatedUser);
            
        } catch (IllegalArgumentException e) {
            log.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            log.info("=== DELETE USER API CALL STARTED ===");
            log.info("Deleting user with ID: {}", id);
            
            userService.deleteUser(id);
            
            log.info("User deleted successfully with ID: {}", id);
            return ResponseEntity.ok("User deleted successfully");
            
        } catch (IllegalArgumentException e) {
            log.error("Error deleting user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error deleting user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred");
        }
    }
}