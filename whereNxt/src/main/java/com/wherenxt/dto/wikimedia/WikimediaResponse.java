package com.wherenxt.dto.wikimedia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WikimediaResponse {

    private WikimediaQuery query;

    public WikimediaResponse() {
    }

    public WikimediaQuery getQuery() {
        return query;
    }

    public void setQuery(WikimediaQuery query) {
        this.query = query;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WikimediaQuery {
        private Map<String, WikimediaPage> pages = new HashMap<>();

        public WikimediaQuery() {
        }

        public Map<String, WikimediaPage> getPages() {
            return pages;
        }

        public void setPages(Map<String, WikimediaPage> pages) {
            this.pages = pages;
        }
    }
}
