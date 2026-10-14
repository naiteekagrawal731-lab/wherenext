package com.wherenxt.dto.wikimedia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WikimediaPage {

    private long pageid;
    private int ns;
    private String title;
    private List<WikimediaImageInfo> imageinfo = new ArrayList<>();

    public WikimediaPage() {
    }

    public long getPageid() {
        return pageid;
    }

    public void setPageid(long pageid) {
        this.pageid = pageid;
    }

    public int getNs() {
        return ns;
    }

    public void setNs(int ns) {
        this.ns = ns;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<WikimediaImageInfo> getImageinfo() {
        return imageinfo;
    }

    public void setImageinfo(List<WikimediaImageInfo> imageinfo) {
        this.imageinfo = imageinfo;
    }
}
