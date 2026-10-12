package com.travel.recommendation.model;

public class CostBreakdownDto {
    private CostItemDto roundTripTransportation;
    private CostItemDto accommodation;
    private CostItemDto food;
    private CostItemDto localTransportation;
    private CostItemDto activitiesAndEntryFees;
    private CostItemDto miscellaneous;
    private CostRangeDto totalTripCost;
    private CostRangeDto perPersonCost;

    public CostBreakdownDto() {
    }

    public CostItemDto getRoundTripTransportation() {
        return roundTripTransportation;
    }

    public void setRoundTripTransportation(CostItemDto roundTripTransportation) {
        this.roundTripTransportation = roundTripTransportation;
    }

    public CostItemDto getAccommodation() {
        return accommodation;
    }

    public void setAccommodation(CostItemDto accommodation) {
        this.accommodation = accommodation;
    }

    public CostItemDto getFood() {
        return food;
    }

    public void setFood(CostItemDto food) {
        this.food = food;
    }

    public CostItemDto getLocalTransportation() {
        return localTransportation;
    }

    public void setLocalTransportation(CostItemDto localTransportation) {
        this.localTransportation = localTransportation;
    }

    public CostItemDto getActivitiesAndEntryFees() {
        return activitiesAndEntryFees;
    }

    public void setActivitiesAndEntryFees(CostItemDto activitiesAndEntryFees) {
        this.activitiesAndEntryFees = activitiesAndEntryFees;
    }

    public CostItemDto getMiscellaneous() {
        return miscellaneous;
    }

    public void setMiscellaneous(CostItemDto miscellaneous) {
        this.miscellaneous = miscellaneous;
    }

    public CostRangeDto getTotalTripCost() {
        return totalTripCost;
    }

    public void setTotalTripCost(CostRangeDto totalTripCost) {
        this.totalTripCost = totalTripCost;
    }

    public CostRangeDto getPerPersonCost() {
        return perPersonCost;
    }

    public void setPerPersonCost(CostRangeDto perPersonCost) {
        this.perPersonCost = perPersonCost;
    }
}
