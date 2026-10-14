package com.wherenxt.exception;

public class GeminiApiException extends ApiException {

    public GeminiApiException(String message) {
        super(message, 502);
    }

    public GeminiApiException(String message, Throwable cause) {
        super(message, cause, 502);
    }

    public GeminiApiException(String message, int statusCode) {
        super(message, statusCode);
    }
}
