package com.travel.recommendation.controller;

import com.travel.recommendation.exception.ErrorResponse;
import com.travel.recommendation.exception.GeminiApiException;
import com.travel.recommendation.exception.GooglePlacesApiException;
import com.travel.recommendation.exception.InvalidResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> details = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        log.warn("Validation failed: {}", details);
        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "Input preferences validation failed. Please check the provided values.",
                HttpStatus.BAD_REQUEST.value(),
                details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(GeminiApiException.class)
    public ResponseEntity<ErrorResponse> handleGeminiError(GeminiApiException ex) {
        log.error("Gemini API error: {}", ex.getMessage(), ex);
        int status = ex.getStatusCode() > 0 ? ex.getStatusCode() : HttpStatus.BAD_GATEWAY.value();
        ErrorResponse errorResponse = new ErrorResponse(
                "GEMINI_API_ERROR",
                ex.getMessage(),
                status
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(GooglePlacesApiException.class)
    public ResponseEntity<ErrorResponse> handlePlacesError(GooglePlacesApiException ex) {
        log.error("Google Places API error: {}", ex.getMessage(), ex);
        int status = ex.getStatusCode() > 0 ? ex.getStatusCode() : HttpStatus.BAD_GATEWAY.value();
        ErrorResponse errorResponse = new ErrorResponse(
                "PLACES_API_ERROR",
                ex.getMessage(),
                status
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(InvalidResponseException.class)
    public ResponseEntity<ErrorResponse> handleInvalidResponse(InvalidResponseException ex) {
        log.error("Invalid response parsed from external service: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
                "EXTERNAL_DATA_ERROR",
                ex.getMessage(),
                HttpStatus.BAD_GATEWAY.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unexpected internal server error: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected server error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
