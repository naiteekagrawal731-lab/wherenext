package com.wherenxt.dto;

public class CostRangeDto {

    private int low;
    private int typical;
    private int high;
    private String currency = "INR";

    public CostRangeDto() {
    }

    public CostRangeDto(int low, int typical, int high) {
        this.low = low;
        this.typical = typical;
        this.high = high;
        this.currency = "INR";
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public int getTypical() {
        return typical;
    }

    public void setTypical(int typical) {
        this.typical = typical;
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
