package com.wherenxt.dto.wikimedia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WikimediaImageInfo {

    private String url;
    private String thumburl;
    private String descriptionurl;
    private String descriptionshorturl;
    private String mime;
    private int width;
    private int height;
    private int thumbwidth;
    private int thumbheight;
    private Map<String, WikimediaMetadataField> extmetadata = new HashMap<>();

    public WikimediaImageInfo() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumburl() {
        return thumburl;
    }

    public void setThumburl(String thumburl) {
        this.thumburl = thumburl;
    }

    public String getDescriptionurl() {
        return descriptionurl;
    }

    public void setDescriptionurl(String descriptionurl) {
        this.descriptionurl = descriptionurl;
    }

    public String getDescriptionshorturl() {
        return descriptionshorturl;
    }

    public void setDescriptionshorturl(String descriptionshorturl) {
        this.descriptionshorturl = descriptionshorturl;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getThumbwidth() {
        return thumbwidth;
    }

    public void setThumbwidth(int thumbwidth) {
        this.thumbwidth = thumbwidth;
    }

    public int getThumbheight() {
        return thumbheight;
    }

    public void setThumbheight(int thumbheight) {
        this.thumbheight = thumbheight;
    }

    public Map<String, WikimediaMetadataField> getExtmetadata() {
        return extmetadata;
    }

    public void setExtmetadata(Map<String, WikimediaMetadataField> extmetadata) {
        this.extmetadata = extmetadata;
    }

    public String getMetadataValue(String key) {
        if (extmetadata != null && extmetadata.containsKey(key)) {
            WikimediaMetadataField field = extmetadata.get(key);
            return field != null ? field.getValue() : null;
        }
        return null;
    }
}
