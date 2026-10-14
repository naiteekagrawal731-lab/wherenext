package com.wherenxt.dto;

public class CostEstimatesDto {

    private CostRangeDto transport;
    private CostRangeDto accommodation;
    private CostRangeDto food;
    private CostRangeDto localTravel;
    private CostRangeDto activities;
    private CostRangeDto miscellaneous;
    private CostRangeDto total;

    public CostEstimatesDto() {
    }

    public CostEstimatesDto(CostRangeDto transport, CostRangeDto accommodation, CostRangeDto food,
                            CostRangeDto localTravel, CostRangeDto activities,
                            CostRangeDto miscellaneous, CostRangeDto total) {
        this.transport = transport;
        this.accommodation = accommodation;
        this.food = food;
        this.localTravel = localTravel;
        this.activities = activities;
        this.miscellaneous = miscellaneous;
        this.total = total;
    }

    public CostRangeDto getTransport() {
        return transport;
    }

    public void setTransport(CostRangeDto transport) {
        this.transport = transport;
    }

    public CostRangeDto getAccommodation() {
        return accommodation;
    }

    public void setAccommodation(CostRangeDto accommodation) {
        this.accommodation = accommodation;
    }

    public CostRangeDto getFood() {
        return food;
    }

    public void setFood(CostRangeDto food) {
        this.food = food;
    }

    public CostRangeDto getLocalTravel() {
        return localTravel;
    }

    public void setLocalTravel(CostRangeDto localTravel) {
        this.localTravel = localTravel;
    }

    public CostRangeDto getActivities() {
        return activities;
    }

    public void setActivities(CostRangeDto activities) {
        this.activities = activities;
    }

    public CostRangeDto getMiscellaneous() {
        return miscellaneous;
    }

    public void setMiscellaneous(CostRangeDto miscellaneous) {
        this.miscellaneous = miscellaneous;
    }

    public CostRangeDto getTotal() {
        return total;
    }

    public void setTotal(CostRangeDto total) {
        this.total = total;
    }
}
