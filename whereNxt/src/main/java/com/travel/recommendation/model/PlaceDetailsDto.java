package com.travel.recommendation.model;

import java.util.ArrayList;
import java.util.List;

public class PlaceDetailsDto {
    private String placeId;
    private String formattedAddress;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private Integer userRatingCount;
    private String googleMapsUrl;
    private String photoUrl;
    private List<PhotoAttributionDto> photoAttributions = new ArrayList<>();
    private boolean photoAvailable = false;
    private String photoUnavailableReason;

    public PlaceDetailsDto() {
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getUserRatingCount() {
        return userRatingCount;
    }

    public void setUserRatingCount(Integer userRatingCount) {
        this.userRatingCount = userRatingCount;
    }

    public String getGoogleMapsUrl() {
        return googleMapsUrl;
    }

    public void setGoogleMapsUrl(String googleMapsUrl) {
        this.googleMapsUrl = googleMapsUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public List<PhotoAttributionDto> getPhotoAttributions() {
        return photoAttributions;
    }

    public void setPhotoAttributions(List<PhotoAttributionDto> photoAttributions) {
        this.photoAttributions = photoAttributions != null ? photoAttributions : new ArrayList<>();
    }

    public boolean isPhotoAvailable() {
        return photoAvailable;
    }

    public void setPhotoAvailable(boolean photoAvailable) {
        this.photoAvailable = photoAvailable;
    }

    public String getPhotoUnavailableReason() {
        return photoUnavailableReason;
    }

    public void setPhotoUnavailableReason(String photoUnavailableReason) {
        this.photoUnavailableReason = photoUnavailableReason;
    }
}
