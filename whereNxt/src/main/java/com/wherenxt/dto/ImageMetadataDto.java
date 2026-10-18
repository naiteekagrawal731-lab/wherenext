package com.wherenxt.dto;

public class ImageMetadataDto {

    private String url;
    private String thumbnailUrl;
    private String title;
    private String description;
    private String artist;
    private String license;
    private String licenseUrl;
    private String sourcePageUrl;
    private String attributionText;

    public ImageMetadataDto() {
    }

    public ImageMetadataDto(String url, String thumbnailUrl, String title, String description,
                            String artist, String license, String licenseUrl,
                            String sourcePageUrl, String attributionText) {
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
        this.description = description;
        this.artist = artist;
        this.license = license;
        this.licenseUrl = licenseUrl;
        this.sourcePageUrl = sourcePageUrl;
        this.attributionText = attributionText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOriginalUrl() {
        return url;
    }

    public void setOriginalUrl(String originalUrl) {
        this.url = originalUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
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

    public String getSourcePageUrl() {
        return sourcePageUrl;
    }

    public void setSourcePageUrl(String sourcePageUrl) {
        this.sourcePageUrl = sourcePageUrl;
    }

    public String getAttributionText() {
        return attributionText;
    }

    public void setAttributionText(String attributionText) {
        this.attributionText = attributionText;
    }
}
