package com.wherenxt.dto;

import java.util.ArrayList;
import java.util.List;

public class ImageSearchResponse {

    private String query;
    private int totalResults;
    private List<ImageMetadataDto> images = new ArrayList<>();

    public ImageSearchResponse() {
    }

    public ImageSearchResponse(String query, int totalResults, List<ImageMetadataDto> images) {
        this.query = query;
        this.totalResults = totalResults;
        this.images = images;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public List<ImageMetadataDto> getImages() {
        return images;
    }

    public void setImages(List<ImageMetadataDto> images) {
        this.images = images;
    }
}
