package com.wherenxt.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class RecommendationRequest {

    @NotBlank(message = "Starting city is required")
    private String startingCity = "Bengaluru";

    @NotNull(message = "Budget is required")
    @Min(value = 5000, message = "Budget must be at least ₹5,000")
    @Max(value = 10000000, message = "Budget exceeds maximum supported limit")
    private Integer budget = 45000;

    private boolean groupBudget = true;

    @NotNull(message = "Number of travelers is required")
    @Min(value = 1, message = "At least 1 traveler is required")
    @Max(value = 50, message = "Maximum 50 travelers supported")
    private Integer travelers = 2;

    @NotNull(message = "Trip length in days is required")
    @Min(value = 1, message = "Trip length must be at least 1 day")
    @Max(value = 60, message = "Trip length cannot exceed 60 days")
    private Integer tripLengthDays = 5;

    @NotEmpty(message = "At least one interest must be selected")
    private List<String> interests = new ArrayList<>();

    private String pace = "Balanced";
    private String transport = "Train + local";
    private String stayStyle = "Boutique hotels";
    private String companion = "Partner";

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes = "";

    @Size(max = 200, message = "Destination preference cannot exceed 200 characters")
    private String destinationPreference = "";

    public RecommendationRequest() {
    }

    public String getStartingCity() {
        return startingCity;
    }

    public void setStartingCity(String startingCity) {
        this.startingCity = startingCity;
    }

    public Integer getBudget() {
        return budget;
    }

    public void setBudget(Integer budget) {
        this.budget = budget;
    }

    public boolean isGroupBudget() {
        return groupBudget;
    }

    public void setGroupBudget(boolean groupBudget) {
        this.groupBudget = groupBudget;
    }

    public Integer getTravelers() {
        return travelers;
    }

    public void setTravelers(Integer travelers) {
        this.travelers = travelers;
    }

    public Integer getTripLengthDays() {
        return tripLengthDays;
    }

    public void setTripLengthDays(Integer tripLengthDays) {
        this.tripLengthDays = tripLengthDays;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public String getPace() {
        return pace;
    }

    public void setPace(String pace) {
        this.pace = pace;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getStayStyle() {
        return stayStyle;
    }

    public void setStayStyle(String stayStyle) {
        this.stayStyle = stayStyle;
    }

    public String getCompanion() {
        return companion;
    }

    public void setCompanion(String companion) {
        this.companion = companion;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDestinationPreference() {
        return destinationPreference;
    }

    public void setDestinationPreference(String destinationPreference) {
        this.destinationPreference = destinationPreference;
    }
}
