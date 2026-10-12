package com.travel.recommendation.model;

import java.util.ArrayList;
import java.util.List;

public class ItineraryDayDto {
    private int day;
    private String title;
    private String description;
    private List<String> activities = new ArrayList<>();
    private Double estimatedCost;

    public ItineraryDayDto() {
    }

    public ItineraryDayDto(int day, String title, String description, List<String> activities, Double estimatedCost) {
        this.day = day;
        this.title = title;
        this.description = description;
        this.activities = activities != null ? activities : new ArrayList<>();
        this.estimatedCost = estimatedCost;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities != null ? activities : new ArrayList<>();
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }
}
