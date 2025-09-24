package com.back.postpilot.service.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerationResult {
    private List<ImageItem> images;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageItem {
        private String urlOrBlobKey;
        private String fileName;
        private String mimeType;
        private Integer width;
        private Integer height;
        private String provider;
        private String model;
        private String seed;
        private String safetyFlags;
    }
}


