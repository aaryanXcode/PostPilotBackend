package com.back.postpilot.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformInfoDTO {
    private String key;
    private String name;
    private String displayName;
    private String description;
    private boolean active;
    private String category; // "social", "professional", "microblogging", "visual"
}
