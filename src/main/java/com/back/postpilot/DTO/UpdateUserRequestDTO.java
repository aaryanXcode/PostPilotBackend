package com.back.postpilot.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDTO(
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,
    
    @Email(message = "Email must be valid")
    String email,
    
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,
    
    String role
) {
}
