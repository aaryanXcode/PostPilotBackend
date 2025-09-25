package com.back.postpilot.service.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service("azureOpenAIImageService")
public class AzureOpenAIImageService implements ImageGenerationService {

    private final ImageModel imageModel;

    @Autowired
    public AzureOpenAIImageService(@Qualifier("azureOpenAiImageModel") ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    @Override
    public ImageGenerationResult generate(ImageGenerationRequest request) {
        try {
            ImagePrompt prompt = new ImagePrompt(request.getPrompt());
            ImageResponse response = imageModel.call(prompt);

            List<ImageGenerationResult.ImageItem> items = response.getResults().stream().map(r ->
                    ImageGenerationResult.ImageItem.builder()
                            .urlOrBlobKey(r.getOutput().getUrl())
                            .mimeType("image/png")
                            .provider("azureopenai")
                            .model(request.getModel())
                            .build()
            ).collect(Collectors.toList());

            return ImageGenerationResult.builder().images(items).build();
        } catch (Exception ex) {
            log.error("Image generation failed", ex);
            return ImageGenerationResult.builder().images(List.of()).build();
        }
    }

    @Override
    public String getProviderKey() {
        return "azureopenai";
    }
}


