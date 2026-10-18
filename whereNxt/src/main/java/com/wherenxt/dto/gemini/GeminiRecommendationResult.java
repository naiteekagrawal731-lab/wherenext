package com.wherenxt.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wherenxt.dto.CostEstimatesDto;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiRecommendationResult {

    private List<GeminiDestinationItem> destinations = new ArrayList<>();
    private boolean fallback = false;
    private String fallbackReason;

    public GeminiRecommendationResult() {
    }

    public List<GeminiDestinationItem> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<GeminiDestinationItem> destinations) {
        this.destinations = destinations;
    }

    public boolean isFallback() {
        return fallback;
    }

    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }

    public String getFallbackReason() {
        return fallbackReason;
    }

    public void setFallbackReason(String fallbackReason) {
        this.fallbackReason = fallbackReason;
    }
}
