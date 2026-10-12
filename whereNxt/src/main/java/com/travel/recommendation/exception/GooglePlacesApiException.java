package com.travel.recommendation.exception;

public class GooglePlacesApiException extends RuntimeException {
    private final int statusCode;

    public GooglePlacesApiException(String message) {
        super(message);
        this.statusCode = 502;
    }

    public GooglePlacesApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 502;
    }

    public GooglePlacesApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
