package com.wherenxt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gemini.api")
public class GeminiProperties {

    private String key = "";
    private String baseUrl = "https://generativelanguage.googleapis.com";
    private String model = "gemini-1.5-flash";
    private int timeoutSeconds = 30;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isKeyConfigured() {
        return key != null && !key.trim().isEmpty() && !key.equalsIgnoreCase("dummy");
    }

    public String getMaskedKey() {
        if (!isKeyConfigured()) {
            return "NOT_CONFIGURED";
        }
        int length = key.length();
        if (length <= 8) {
            return "****";
        }
        return key.substring(0, 4) + "..." + key.substring(length - 4);
    }
}
