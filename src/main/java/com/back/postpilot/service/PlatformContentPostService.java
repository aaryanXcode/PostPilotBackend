package com.back.postpilot.service;

import com.back.postpilot.DTO.GenerateContentDTO;
import com.back.postpilot.EnumTypeConstants.ContentPlatForms;
import com.back.postpilot.entity.GeneratedContent;
import org.springframework.http.HttpEntity;

import java.io.IOException;

public interface PlatformContentPostService {
    String postContent(GenerateContentDTO generateContentDTO) throws Exception;
    ContentPlatForms getSupportedPlatform();
}
