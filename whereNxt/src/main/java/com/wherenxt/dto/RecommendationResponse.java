package com.wherenxt.dto;

import java.util.ArrayList;
import java.util.List;

public class RecommendationResponse {

    private String startingCity;
    private int tripLengthDays;
    private int travelers;
    private int totalBudget;
    private int budgetPerPerson;
    private String currency = "INR";
    private String generatedAt;
    private String source;
    private boolean fallback;
    private String fallbackReason;
    private List<DestinationDto> destinations = new ArrayList<>();

    public RecommendationResponse() {
    }

    public String getStartingCity() {
        return startingCity;
    }

    public void setStartingCity(String startingCity) {
        this.startingCity = startingCity;
    }

    public int getTripLengthDays() {
        return tripLengthDays;
    }

    public void setTripLengthDays(int tripLengthDays) {
        this.tripLengthDays = tripLengthDays;
    }

    public int getTravelers() {
        return travelers;
    }

    public void setTravelers(int travelers) {
        this.travelers = travelers;
    }

    public int getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(int totalBudget) {
        this.totalBudget = totalBudget;
    }

    public int getBudgetPerPerson() {
        return budgetPerPerson;
    }

    public void setBudgetPerPerson(int budgetPerPerson) {
        this.budgetPerPerson = budgetPerPerson;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    public List<DestinationDto> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<DestinationDto> destinations) {
        this.destinations = destinations;
    }
}
