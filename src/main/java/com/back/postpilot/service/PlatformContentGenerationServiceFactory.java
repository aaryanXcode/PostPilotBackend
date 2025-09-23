package com.back.postpilot.service;

import com.back.postpilot.EnumTypeConstants.AssitanceModels;
import com.back.postpilot.EnumTypeConstants.ContentPlatForms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.back.postpilot.EnumTypeConstants.ContentPlatForms.*;

@Component
public class PlatformContentGenerationServiceFactory {

    private final Map<ContentPlatForms, PlatformContentGenerationService> services = new HashMap<>();

    @Autowired
    public PlatformContentGenerationServiceFactory(List<PlatformContentGenerationService> serviceList) {
        serviceList.forEach(service ->
                services.put(service.getSupportedPlatform(), service)
        );
    }

    public PlatformContentGenerationService getPlatformService(ContentPlatForms platform) {
        PlatformContentGenerationService service = services.get(platform);
        if (service == null) {
            throw new IllegalArgumentException("Unsupported platform: " + platform);
        }
        return service;
    }
}
