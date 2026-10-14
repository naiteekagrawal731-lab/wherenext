package com.wherenxt.exception;

public class WikimediaApiException extends ApiException {

    public WikimediaApiException(String message) {
        super(message, 502);
    }

    public WikimediaApiException(String message, Throwable cause) {
        super(message, cause, 502);
    }

    public WikimediaApiException(String message, int statusCode) {
        super(message, statusCode);
    }
}
