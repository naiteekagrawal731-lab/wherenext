package com.wherenxt.dto;

public class HealthResponse {

    private String status;
    private String service;
    private String timestamp;
    private boolean geminiConfigured;
    private String version;

    public HealthResponse() {
    }

    public HealthResponse(String status, String service, String timestamp, boolean geminiConfigured, String version) {
        this.status = status;
        this.service = service;
        this.timestamp = timestamp;
        this.geminiConfigured = geminiConfigured;
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isGeminiConfigured() {
        return geminiConfigured;
    }

    public void setGeminiConfigured(boolean geminiConfigured) {
        this.geminiConfigured = geminiConfigured;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
