package com.back.postpilot.DTO;

import com.back.postpilot.entity.Role;

public record UserResponseDTO(
    Long id,
    String username,
    String email,
    String name,
    Role role
) {
}
