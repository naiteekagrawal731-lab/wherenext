package com.travel.recommendation.model;

public class PhotoAttributionDto {
    private String authorName;
    private String authorUri;

    public PhotoAttributionDto() {
    }

    public PhotoAttributionDto(String authorName, String authorUri) {
        this.authorName = authorName;
        this.authorUri = authorUri;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorUri() {
        return authorUri;
    }

    public void setAuthorUri(String authorUri) {
        this.authorUri = authorUri;
    }
}
