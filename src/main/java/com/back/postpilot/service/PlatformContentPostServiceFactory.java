package com.back.postpilot.service;

import com.back.postpilot.EnumTypeConstants.ContentPlatForms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PlatformContentPostServiceFactory {
    private final Map<ContentPlatForms, PlatformContentPostService> services = new HashMap<>();

    @Autowired
    public PlatformContentPostServiceFactory(List<PlatformContentPostService> serviceList) {
        serviceList.forEach(service ->
                services.put(service.getSupportedPlatform(), service)
        );
    }

    public PlatformContentPostService getPlatformService(ContentPlatForms platform) {
        PlatformContentPostService service = services.get(platform);
        if (service == null) {
            throw new IllegalArgumentException("Unsupported platform: " + platform);
        }
        return service;
    }
}
