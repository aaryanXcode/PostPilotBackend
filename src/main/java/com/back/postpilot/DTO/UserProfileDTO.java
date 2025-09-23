package com.back.postpilot.DTO;

import com.back.postpilot.entity.Role;

public record UserProfileDTO(Long Id, String userName, Role role, String email) {
}
