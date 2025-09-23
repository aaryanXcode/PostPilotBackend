package com.back.postpilot.domain;

import com.back.postpilot.DTO.GenerateContentDTO;
import com.back.postpilot.EnumTypeConstants.ContentStatus;
import com.back.postpilot.EnumTypeConstants.ContentType;

import java.time.LocalDateTime;
import java.util.List;

public class LinkedInContentGenerator implements PlatformContentGenerator{

    @Override
    public GenerateContentDTO generatedContentDto(ContentGenerationRequest request){
        return GenerateContentDTO.builder()
                .id(null) // will be assigned when persisted
                .title("LinkedIn Post: " + request.getPrompt())
                .content(null)
                .hashtags("#LinkedIn #AI")
                .createdAt(LocalDateTime.now())
                .contentType(ContentType.POST)
                .status(ContentStatus.DRAFT)
                .imageUrls(List.of())
                .metadata("{\"platform\": \"LinkedIn\"}")
                .build();
    }
}

/*
* two approaches of flow, use request and direct provide to Ai and then create GeneratedContent object using builder save to DB and return
* controller(request)-> service(AI provider, ContentGenerator(request, AiProvider)->return GeneratedContent.save()
* use request set some value for content specific and then pass that into AI and generte content save and return
* controller(request)-> service(AI provider, PlatformCOntentgenerator(request)->generateContent()-> return LinkedINContent  -> Ai Provider(LinkedinContent) -> GeneratedContent.save()
 */
