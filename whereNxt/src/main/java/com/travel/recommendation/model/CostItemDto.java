package com.travel.recommendation.model;

public class CostItemDto {
    private Double estimatedCost;
    private String notes;

    public CostItemDto() {
    }

    public CostItemDto(Double estimatedCost, String notes) {
        this.estimatedCost = estimatedCost;
        this.notes = notes;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
