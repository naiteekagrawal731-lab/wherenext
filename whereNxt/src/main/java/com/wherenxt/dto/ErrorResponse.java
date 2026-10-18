package com.wherenxt.dto;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {

    private boolean success = false;
    private String timestamp;
    private int status;
    private String error;
    private String errorCode;
    private String message;
    private boolean retryable = false;
    private List<String> details = new ArrayList<>();

    public ErrorResponse() {
    }

    public ErrorResponse(String timestamp, int status, String error, String message, List<String> details) {
        this(timestamp, status, error, error != null ? error.toUpperCase().replace(" ", "_") : "ERROR", message, false, details);
    }

    public ErrorResponse(String timestamp, int status, String error, String errorCode, String message, boolean retryable, List<String> details) {
        this.success = false;
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.errorCode = errorCode;
        this.message = message;
        this.retryable = retryable;
        this.details = details != null ? details : new ArrayList<>();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
}
