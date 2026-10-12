package com.travel.recommendation.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {
    private String error;
    private String message;
    private int status;
    private String timestamp = Instant.now().toString();
    private List<String> details = new ArrayList<>();

    public ErrorResponse() {
    }

    public ErrorResponse(String error, String message, int status) {
        this.error = error;
        this.message = message;
        this.status = status;
    }

    public ErrorResponse(String error, String message, int status, List<String> details) {
        this.error = error;
        this.message = message;
        this.status = status;
        this.details = details != null ? details : new ArrayList<>();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details != null ? details : new ArrayList<>();
    }
}
