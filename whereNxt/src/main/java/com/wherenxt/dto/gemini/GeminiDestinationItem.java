package com.wherenxt.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wherenxt.dto.CostEstimatesDto;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiDestinationItem {

    private String place;
    private String state;
    private int match = 90;
    private String recommendedDuration;
    private String summary;
    private List<String> tags = new ArrayList<>();
    private List<String> activities = new ArrayList<>();
    private List<String> travelTips = new ArrayList<>();
    private CostEstimatesDto costEstimates;
    private List<GeminiDayPlan> dayByDayItinerary = new ArrayList<>();
    private List<String> assumptions = new ArrayList<>();
    private List<String> uncertaintyMarkers = new ArrayList<>();

    public GeminiDestinationItem() {
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getMatch() {
        return match;
    }

    public void setMatch(int match) {
        this.match = match;
    }

    public String getRecommendedDuration() {
        return recommendedDuration;
    }

    public void setRecommendedDuration(String recommendedDuration) {
        this.recommendedDuration = recommendedDuration;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    public List<String> getTravelTips() {
        return travelTips;
    }

    public void setTravelTips(List<String> travelTips) {
        this.travelTips = travelTips;
    }

    public CostEstimatesDto getCostEstimates() {
        return costEstimates;
    }

    public void setCostEstimates(CostEstimatesDto costEstimates) {
        this.costEstimates = costEstimates;
    }

    public List<GeminiDayPlan> getDayByDayItinerary() {
        return dayByDayItinerary;
    }

    public void setDayByDayItinerary(List<GeminiDayPlan> dayByDayItinerary) {
        this.dayByDayItinerary = dayByDayItinerary;
    }

    public List<String> getAssumptions() {
        return assumptions;
    }

    public void setAssumptions(List<String> assumptions) {
        this.assumptions = assumptions;
    }

    public List<String> getUncertaintyMarkers() {
        return uncertaintyMarkers;
    }

    public void setUncertaintyMarkers(List<String> uncertaintyMarkers) {
        this.uncertaintyMarkers = uncertaintyMarkers;
    }
}
