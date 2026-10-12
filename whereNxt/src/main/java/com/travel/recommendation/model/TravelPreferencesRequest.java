package com.travel.recommendation.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;

public class TravelPreferencesRequest {

    @NotBlank(message = "Starting city is required")
    private String startingCity;

    @NotBlank(message = "Budget type must be specified (TOTAL or PER_PERSON)")
    private String budgetType = "TOTAL"; // TOTAL or PER_PERSON

    @NotNull(message = "Budget amount is required")
    @Positive(message = "Budget amount must be greater than zero")
    private Double budgetAmount;

    @Min(value = 1, message = "At least one traveler is required")
    private int travelersCount = 1;

    @Min(value = 1, message = "Trip duration must be at least 1 day")
    private int durationDays = 3;

    private String startDate;
    private String endDate;

    private List<String> interests = new ArrayList<>();
    private String travelStyle = "Mid-range"; // Budget, Mid-range, Luxury, Backpacking, Relaxed, Active
    private String preferredTransportation = "Flexible"; // Flight, Train, Bus, Self-drive, Flexible
    private String accommodationPreference = "Mid-range hotel"; // Hostel, Homestay, Budget hotel, Mid-range hotel, Luxury resort
    private String companions = "Solo"; // Solo, Couple, Family, Friends, Group
    private String travelPace = "Moderate"; // Relaxed, Moderate, Fast-paced

    public TravelPreferencesRequest() {
    }

    public String getStartingCity() {
        return startingCity;
    }

    public void setStartingCity(String startingCity) {
        this.startingCity = startingCity;
    }

    public String getBudgetType() {
        return budgetType;
    }

    public void setBudgetType(String budgetType) {
        this.budgetType = budgetType;
    }

    public Double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(Double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public int getTravelersCount() {
        return travelersCount;
    }

    public void setTravelersCount(int travelersCount) {
        this.travelersCount = travelersCount;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests != null ? interests : new ArrayList<>();
    }

    public String getTravelStyle() {
        return travelStyle;
    }

    public void setTravelStyle(String travelStyle) {
        this.travelStyle = travelStyle;
    }

    public String getPreferredTransportation() {
        return preferredTransportation;
    }

    public void setPreferredTransportation(String preferredTransportation) {
        this.preferredTransportation = preferredTransportation;
    }

    public String getAccommodationPreference() {
        return accommodationPreference;
    }

    public void setAccommodationPreference(String accommodationPreference) {
        this.accommodationPreference = accommodationPreference;
    }

    public String getCompanions() {
        return companions;
    }

    public void setCompanions(String companions) {
        this.companions = companions;
    }

    public String getTravelPace() {
        return travelPace;
    }

    public void setTravelPace(String travelPace) {
        this.travelPace = travelPace;
    }
}
