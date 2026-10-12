package com.travel.recommendation.model;

import java.util.ArrayList;
import java.util.List;

public class DestinationRecommendationDto {
    private String name;
    private String state;
    private String description;
    private List<String> recommendationReasons = new ArrayList<>();
    private List<String> matchingInterests = new ArrayList<>();
    private List<String> suggestedActivities = new ArrayList<>();
    private List<ItineraryDayDto> itinerary = new ArrayList<>();
    private CostBreakdownDto costBreakdown;
    private List<String> assumptionsAndUncertainties = new ArrayList<>();
    private PlaceDetailsDto placeDetails;

    public DestinationRecommendationDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRecommendationReasons() {
        return recommendationReasons;
    }

    public void setRecommendationReasons(List<String> recommendationReasons) {
        this.recommendationReasons = recommendationReasons != null ? recommendationReasons : new ArrayList<>();
    }

    public List<String> getMatchingInterests() {
        return matchingInterests;
    }

    public void setMatchingInterests(List<String> matchingInterests) {
        this.matchingInterests = matchingInterests != null ? matchingInterests : new ArrayList<>();
    }

    public List<String> getSuggestedActivities() {
        return suggestedActivities;
    }

    public void setSuggestedActivities(List<String> suggestedActivities) {
        this.suggestedActivities = suggestedActivities != null ? suggestedActivities : new ArrayList<>();
    }

    public List<ItineraryDayDto> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<ItineraryDayDto> itinerary) {
        this.itinerary = itinerary != null ? itinerary : new ArrayList<>();
    }

    public CostBreakdownDto getCostBreakdown() {
        return costBreakdown;
    }

    public void setCostBreakdown(CostBreakdownDto costBreakdown) {
        this.costBreakdown = costBreakdown;
    }

    public List<String> getAssumptionsAndUncertainties() {
        return assumptionsAndUncertainties;
    }

    public void setAssumptionsAndUncertainties(List<String> assumptionsAndUncertainties) {
        this.assumptionsAndUncertainties = assumptionsAndUncertainties != null ? assumptionsAndUncertainties : new ArrayList<>();
    }

    public PlaceDetailsDto getPlaceDetails() {
        return placeDetails;
    }

    public void setPlaceDetails(PlaceDetailsDto placeDetails) {
        this.placeDetails = placeDetails;
    }
}
