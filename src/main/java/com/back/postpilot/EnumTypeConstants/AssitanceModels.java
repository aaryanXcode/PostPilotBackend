package com.back.postpilot.EnumTypeConstants;

import java.util.HashMap;
import java.util.Map;

public enum AssitanceModels {
    CLAUDE("claude"),
    GEMINI("gemini"),
    CHATGPT("chatgpt"),
    OLLAMA("ollama");

    private final String key;

    AssitanceModels(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    private static final Map<String, AssitanceModels> lookup = new HashMap<>();
    static {
        for (AssitanceModels model : AssitanceModels.values()) {
            lookup.put(model.key, model);
        }
    }

    public static AssitanceModels fromString(String model) {
        if (model == null) {
            throw new IllegalArgumentException("Model cannot be null");
        }
        String lower = model.toLowerCase();

        // exact match first
        if (lookup.containsKey(lower)) {
            return lookup.get(lower);
        }

        // Special handling for ChatGPT models
        if (lower.contains("gpt") || lower.contains("chatgpt") || lower.contains("openai")) {
            System.out.println("DEBUG: ChatGPT model detected: " + model);
            return CHATGPT;
        }

        // fallback: match by contains
        for (AssitanceModels m : values()) {
            if (lower.contains(m.key)) {
                System.out.println("DEBUG: Contains match found: " + m);
                return m;
            }
        }
        throw new IllegalArgumentException("Unsupported model: " + model);
    }
}
