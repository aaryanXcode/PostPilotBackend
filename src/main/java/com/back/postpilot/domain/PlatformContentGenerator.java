package com.back.postpilot.domain;


import com.back.postpilot.DTO.GenerateContentDTO;
import com.back.postpilot.entity.GeneratedContent;
import com.back.postpilot.entity.SocialMediaPlatForm;

public interface PlatformContentGenerator {

    GenerateContentDTO generatedContentDto(ContentGenerationRequest request);
}
