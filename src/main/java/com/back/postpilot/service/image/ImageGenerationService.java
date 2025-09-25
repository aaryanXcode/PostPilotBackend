package com.back.postpilot.service.image;

public interface ImageGenerationService {
    ImageGenerationResult generate(ImageGenerationRequest request);
    String getProviderKey();
}


