package com.wherenxt.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiDayPlan {

    private String day;
    private String title;
    private String detail;

    public GeminiDayPlan() {
    }

    public GeminiDayPlan(String day, String title, String detail) {
        this.day = day;
        this.title = title;
        this.detail = detail;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
