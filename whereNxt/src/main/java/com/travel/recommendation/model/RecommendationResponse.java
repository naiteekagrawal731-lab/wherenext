package com.travel.recommendation.model;

import java.util.ArrayList;
import java.util.List;

public class RecommendationResponse {
    private TravelPreferencesRequest userPreferences;
    private List<DestinationRecommendationDto> destinations = new ArrayList<>();
    private String disclaimer = "All prices are Gemini-generated estimates based on general market rates, not verified live fares or booking quotes. Fares and tariffs may vary based on exact dates, availability, and provider policies.";
    private List<String> warnings = new ArrayList<>();

    public RecommendationResponse() {
    }

    public RecommendationResponse(TravelPreferencesRequest userPreferences, List<DestinationRecommendationDto> destinations) {
        this.userPreferences = userPreferences;
        this.destinations = destinations != null ? destinations : new ArrayList<>();
    }

    public TravelPreferencesRequest getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(TravelPreferencesRequest userPreferences) {
        this.userPreferences = userPreferences;
    }

    public List<DestinationRecommendationDto> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<DestinationRecommendationDto> destinations) {
        this.destinations = destinations != null ? destinations : new ArrayList<>();
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }
}
