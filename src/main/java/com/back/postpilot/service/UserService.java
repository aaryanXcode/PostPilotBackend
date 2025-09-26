package com.back.postpilot.service;

import com.back.postpilot.DTO.CreateUserRequestDTO;
import com.back.postpilot.DTO.UpdateUserRequestDTO;
import com.back.postpilot.DTO.UserResponseDTO;
import com.back.postpilot.entity.Role;
import com.back.postpilot.entity.UserEntity;
import com.back.postpilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO createUser(CreateUserRequestDTO request) {
        log.info("Creating new user with username: {}", request.username());
        
        // Validate required fields are not empty
        if (request.username() == null || request.username().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required and cannot be empty");
        }
        if (request.password() == null || request.password().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required and cannot be empty");
        }
        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required and cannot be empty");
        }
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required and cannot be empty");
        }
        if (request.role() == null || request.role().trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required and cannot be empty");
        }
        
        // Check if username already exists
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.username());
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }
        
        // Validate role
        Role role;
        try {
            role = Role.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.role());
        }
        
        // Create new user entity
        UserEntity user = new UserEntity(
            request.username(),
            passwordEncoder.encode(request.password()),
            request.email(),
            request.name(),
            role
        );
        
        UserEntity savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        
        return new UserResponseDTO(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getName(),
            savedUser.getRole()
        );
    }

    public List<UserResponseDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
            .map(user -> new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getRole()
            ))
            .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);
        UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        return new UserResponseDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getName(),
            user.getRole()
        );
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UpdateUserRequestDTO request) {
        log.info("=== UPDATE USER API CALL STARTED ===");
        log.info("Updating user with ID: {}", id);
        
        // Find the user
        UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        // Check if username is being changed and if it already exists
        if (request.username() != null && !request.username().trim().isEmpty() && 
            !user.getUsername().equals(request.username())) {
            if (userRepository.findByUsername(request.username()).isPresent()) {
                throw new IllegalArgumentException("Username already exists: " + request.username());
            }
            user.setUsername(request.username().trim());
        }
        
        // Check if email is being changed and if it already exists
        if (request.email() != null && !request.email().trim().isEmpty() && 
            !user.getEmail().equals(request.email())) {
            if (userRepository.findByEmail(request.email()).isPresent()) {
                throw new IllegalArgumentException("Email already exists: " + request.email());
            }
            user.setEmail(request.email().trim());
        }
        
        // Update other fields if provided
        if (request.name() != null && !request.name().trim().isEmpty()) {
            user.setName(request.name().trim());
        }
        
        if (request.password() != null && !request.password().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        
        if (request.role() != null && !request.role().trim().isEmpty()) {
            try {
                Role role = Role.valueOf(request.role().toUpperCase());
                user.setRole(role);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + request.role());
            }
        }
        
        UserEntity savedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", savedUser.getId());
        
        return new UserResponseDTO(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getName(),
            savedUser.getRole()
        );
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("=== DELETE USER API CALL STARTED ===");
        log.info("Deleting user with ID: {}", id);
        
        UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        userRepository.delete(user);
        log.info("User deleted successfully with ID: {}", id);
    }
}
