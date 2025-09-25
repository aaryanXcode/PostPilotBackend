package com.back.postpilot.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentPostDTO {
    private Long id;
    private String title;
    private Long engagement;
    private Long reach;
    private Long impressions;
    private String status;
    private String createdAt;
}

