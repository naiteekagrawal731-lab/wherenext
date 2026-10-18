package com.wherenxt.exception;

public class GeminiApiException extends ApiException {

    private final String errorCode;
    private final boolean retryable;

    public GeminiApiException(String message) {
        this(message, 502, "GEMINI_ERROR", false);
    }

    public GeminiApiException(String message, Throwable cause) {
        this(message, cause, 502, "GEMINI_ERROR", false);
    }

    public GeminiApiException(String message, int statusCode) {
        this(message, statusCode, "GEMINI_ERROR", false);
    }

    public GeminiApiException(String message, int statusCode, String errorCode, boolean retryable) {
        super(message, statusCode);
        this.errorCode = errorCode != null ? errorCode : "GEMINI_ERROR";
        this.retryable = retryable;
    }

    public GeminiApiException(String message, Throwable cause, int statusCode, String errorCode, boolean retryable) {
        super(message, cause, statusCode);
        this.errorCode = errorCode != null ? errorCode : "GEMINI_ERROR";
        this.retryable = retryable;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
