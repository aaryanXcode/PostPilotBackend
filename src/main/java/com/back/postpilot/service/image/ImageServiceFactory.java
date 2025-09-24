package com.back.postpilot.service.image;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ImageServiceFactory {

    private final Map<String, ImageGenerationService> services = new HashMap<>();

    public ImageServiceFactory(List<ImageGenerationService> serviceList) {
        serviceList.forEach(s -> services.put(s.getProviderKey(), s));
    }

    public ImageGenerationService getService(String providerKey) {
        ImageGenerationService service = services.get(providerKey);
        if (service == null) {
            throw new IllegalArgumentException("Unsupported image provider: " + providerKey);
        }
        return service;
    }
}


