package com.back.postpilot.service.image;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageGenerationRequest {
    private String prompt;
    private String negativePrompt;
    private String model; // provider/model hint
    private int numberOfImages;
    private String mimeType; // e.g. image/jpeg
    private Integer width;
    private Integer height;
    private Long userId;
    private String sessionId;
}


