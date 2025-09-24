package com.back.postpilot.service;

import com.back.postpilot.domain.ContentGenerationRequest;
import com.back.postpilot.entity.ContentImage;
import com.back.postpilot.entity.GeneratedContent;
import com.back.postpilot.service.image.ImageGenerationRequest;
import com.back.postpilot.service.image.ImageGenerationResult;
import com.back.postpilot.service.image.ImageServiceFactory;
import com.back.postpilot.service.image.LinkedInImagePostGenerationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageOrchestrator {

    private final ImageServiceFactory imageServiceFactory;
    private final LinkedInImagePostGenerationService linkedInImagePostGenerationService;

    public ImageOrchestrator(ImageServiceFactory imageServiceFactory,
                             LinkedInImagePostGenerationService linkedInImagePostGenerationService) {
        this.imageServiceFactory = imageServiceFactory;
        this.linkedInImagePostGenerationService = linkedInImagePostGenerationService;
    }

    public List<ContentImage> generateAndPersistImages(ContentGenerationRequest request,
                                                       GeneratedContent generatedContent) {

        // Provider fallback order: Azure → A4F → Bytez
        String providerKey = "azureopenai";

        String prompt;
        if (linkedInImagePostGenerationService.supports(generatedContent.getPlatform(), generatedContent.getContentType())) {
            prompt = linkedInImagePostGenerationService.buildImagePrompt(request);
        } else {
            prompt = request.getPrompt();
        }

        ImageGenerationRequest imageReq = ImageGenerationRequest.builder()
                .prompt(prompt)
                .model(request.getModel())
                .numberOfImages(1)
                .mimeType("image/png")
                .userId(request.getUserId())
                .sessionId(request.getSessionId())
                .width(1024)
                .height(1024)
                .build();

        ImageGenerationResult result = imageServiceFactory.getService(providerKey).generate(imageReq);
        if (result.getImages() == null || result.getImages().isEmpty()) {
            providerKey = "a4f";
            result = imageServiceFactory.getService(providerKey).generate(imageReq);
            if (result.getImages() == null || result.getImages().isEmpty()) {
                providerKey = "bytez";
                result = imageServiceFactory.getService(providerKey).generate(imageReq);
            }
        }

        List<ContentImage> saved = new ArrayList<>();
        if (result != null && result.getImages() != null) {
            // Ensure images list on GeneratedContent is mutable and initialized
            List<ContentImage> imagesList = generatedContent.getImages();
            if (imagesList == null) {
                imagesList = new ArrayList<>();
                generatedContent.setImages(imagesList);
            } else if (!(imagesList instanceof ArrayList)) {
                imagesList = new ArrayList<>(imagesList);
                generatedContent.setImages(imagesList);
            }
            for (ImageGenerationResult.ImageItem item : result.getImages()) {
                if (item == null) continue;
                String url = item.getUrlOrBlobKey();
                if (url == null || url.isBlank()) continue;

                ContentImage ci = new ContentImage();
                ci.setGeneratedContent(generatedContent);
                ci.setImageUrl(url);
                ci.setImagePrompt(prompt);
                // Hardcode filename for now as requested
                ci.setFileName("generated_image.png");
                String alt = generatedContent.getTitle();
                if (alt == null || alt.isBlank()) alt = prompt;
                ci.setAltText(alt);
                ci.setFileSize(null);
                ci.setGeneratedAt(LocalDateTime.now());
                imagesList.add(ci);
                saved.add(ci);
            }
        }

        return saved;
    }

    private String deriveFileName(String url) {
        try {
            int q = url.indexOf('?');
            String clean = q > -1 ? url.substring(0, q) : url;
            int slash = clean.lastIndexOf('/');
            String name = slash > -1 ? clean.substring(slash + 1) : clean;
            return (name == null || name.isBlank()) ? "image.png" : name;
        } catch (Exception e) {
            return "image.png";
        }
    }
}


