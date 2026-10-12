package com.travel.recommendation.model;

public class CostRangeDto {
    private Double low;
    private Double typical;
    private Double high;
    private String notes;

    public CostRangeDto() {
    }

    public CostRangeDto(Double low, Double typical, Double high, String notes) {
        this.low = low;
        this.typical = typical;
        this.high = high;
        this.notes = notes;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getTypical() {
        return typical;
    }

    public void setTypical(Double typical) {
        this.typical = typical;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
