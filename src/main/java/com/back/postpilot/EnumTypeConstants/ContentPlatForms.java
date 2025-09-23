package com.back.postpilot.EnumTypeConstants;

import java.util.HashSet;
import java.util.Set;

public enum ContentPlatForms {
    INSTAGRAM("Instagram"),
    FACEBOOK("Facebook"),
    LINKEDIN("LinkedIn"),
    TWITTER("Twitter"),
    DEFAULT("default");

    private final String platform;
    ContentPlatForms(String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }

    private static final Set<String> platformSet = new HashSet<>();

    static {
        for (ContentPlatForms cp : ContentPlatForms.values()) {
            platformSet.add(cp.platform.toLowerCase());
        }
    }

    public static boolean containsPlatform(String platform) {
        if (platform == null) {
            return false;
        }
        return platformSet.contains(platform.toLowerCase());
    }
}
