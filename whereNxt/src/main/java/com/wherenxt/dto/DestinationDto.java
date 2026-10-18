package com.wherenxt.dto;

import java.util.ArrayList;
import java.util.List;

public class DestinationDto {

    private long id;
    private String place;
    private String state;
    private int match;
    private String duration;
    private String cost;
    private int costNum;
    private String image;
    private String credit;
    private String handle;
    private String sourceUrl;
    private String license;
    private String licenseUrl;
    private List<String> tags = new ArrayList<>();
    private String summary;
    private List<Integer> costs = new ArrayList<>();
    private List<List<String>> itinerary = new ArrayList<>();
    private CostEstimatesDto costEstimates;
    private List<String> activities = new ArrayList<>();
    private List<String> travelTips = new ArrayList<>();
    private List<String> assumptions = new ArrayList<>();
    private List<String> uncertaintyMarkers = new ArrayList<>();
    private ImageMetadataDto imageMetadata;

    public DestinationDto() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public int getCostNum() {
        return costNum;
    }

    public void setCostNum(int costNum) {
        this.costNum = costNum;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Integer> getCosts() {
        return costs;
    }

    public void setCosts(List<Integer> costs) {
        this.costs = costs;
    }

    public List<List<String>> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<List<String>> itinerary) {
        this.itinerary = itinerary;
    }

    public CostEstimatesDto getCostEstimates() {
        return costEstimates;
    }

    public void setCostEstimates(CostEstimatesDto costEstimates) {
        this.costEstimates = costEstimates;
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

    public ImageMetadataDto getImageMetadata() {
        return imageMetadata;
    }

    public void setImageMetadata(ImageMetadataDto imageMetadata) {
        this.imageMetadata = imageMetadata;
    }
}
