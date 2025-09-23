package com.back.postpilot.EnumTypeConstants;

public enum ContentType {
    TEXT("Text"),
    IMAGE("Image"),
    VIDEO("Video"),
    POST("Post"),
    STORY("Story"),
    ARTICLE("Article"),
    THREAD("Thread"),
    CAROUSEL("Carousel"),
    VIDEO_SCRIPT("Video Script"),
    BLOG_POST("Blog Post");

    private final String label;

    ContentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
