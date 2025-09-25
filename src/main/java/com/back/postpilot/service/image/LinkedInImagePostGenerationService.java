package com.back.postpilot.service.image;

import com.back.postpilot.EnumTypeConstants.ContentPlatForms;
import com.back.postpilot.EnumTypeConstants.ContentType;
import com.back.postpilot.domain.ContentGenerationRequest;
import org.springframework.stereotype.Service;

@Service
public class LinkedInImagePostGenerationService {

    public boolean supports(ContentPlatForms platform, ContentType contentType) {
        return platform == ContentPlatForms.LINKEDIN && contentType == ContentType.IMAGE;
    }

    public String buildImagePrompt(ContentGenerationRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Create a professional LinkedIn-ready image that aligns with the following brief. ");
        sb.append("Strong readability, high contrast, clean composition. Avoid heavy text on image. ");
        sb.append("Brand-safe colors, minimalistic background.\n\n");
        sb.append("Topic: ").append(request.getPrompt()).append("\n");
        if (request.getContext() != null && !request.getContext().isBlank()) {
            sb.append("Context: ").append(request.getContext()).append("\n");
        }
        if (request.getTargetAudience() != null) {
            sb.append("Target audience: ").append(request.getTargetAudience()).append("\n");
        }
        if (request.getTone() != null) {
            sb.append("Tone: ").append(request.getTone()).append("\n");
        }
        sb.append("Output: 1024x1024, photo-realistic or clean vector depending on topic.");
        return sb.toString();
    }
}


