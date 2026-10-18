package com.wherenxt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wikimedia.api")
public class WikimediaProperties {

    private String baseUrl = "https://commons.wikimedia.org/w/api.php";
    private String userAgent = "whereNxt-TravelApp/1.0 (https://github.com/wherenxt; travel-app@wherenxt.com)";
    private int timeoutSeconds = 10;
    private int defaultThumbnailWidth = 800;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getDefaultThumbnailWidth() {
        return defaultThumbnailWidth;
    }

    public void setDefaultThumbnailWidth(int defaultThumbnailWidth) {
        this.defaultThumbnailWidth = defaultThumbnailWidth;
    }
}
