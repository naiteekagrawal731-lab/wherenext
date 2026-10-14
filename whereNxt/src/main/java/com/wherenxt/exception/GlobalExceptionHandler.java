package com.wherenxt.exception;

import com.wherenxt.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> details = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.add(error.getField() + ": " + error.getDefaultMessage());
        }

        ErrorResponse response = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Input preference validation failed",
                details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = new ArrayList<>();
        ex.getConstraintViolations().forEach(violation ->
                details.add(violation.getPropertyPath() + ": " + violation.getMessage()));

        ErrorResponse response = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Constraint Violation",
                "Request parameter violation",
                details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", sanitize(ex.getMessage()));
        ErrorResponse response = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                sanitize(ex.getMessage()),
                List.of(sanitize(ex.getMessage()))
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(GeminiApiException.class)
    public ResponseEntity<ErrorResponse> handleGeminiException(GeminiApiException ex) {
        log.error("Gemini API error occurred: {}", sanitize(ex.getMessage()));
        ErrorResponse response = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.BAD_GATEWAY.value(),
                "Gemini Integration Error",
                "Unable to retrieve travel recommendations from AI service at this moment",
                List.of(sanitize(ex.getMessage()))
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(WikimediaApiException.class)
    public ResponseEntity<ErrorResponse> handleWikimediaException(WikimediaApiException ex) {
        log.error("Wikimedia Commons API error occurred: {}", sanitize(ex.getMessage()));
        ErrorResponse response = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.BAD_GATEWAY.value(),
                "Wikimedia Integration Error",
                "Failed to fetch image data from Wikimedia Commons",
                List.of(sanitize(ex.getMessage()))
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleGenericApiException(ApiException ex) {
        log.error("API error [status {}]: {}", ex.getStatusCode(), sanitize(ex.getMessage()));
        ErrorResponse response = new ErrorResponse(
                Instant.now().toString(),
                ex.getStatusCode(),
                "Service Error",
                sanitize(ex.getMessage()),
                List.of(sanitize(ex.getMessage()))
        );
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUncaughtException(Exception ex) {
        log.error("Unexpected server exception", ex);
        ErrorResponse response = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected server error occurred. Please try again later.",
                List.of("An unexpected internal condition was encountered.")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String sanitize(String input) {
        if (input == null) return "Unknown";
        // Scrub potential API key strings or secrets from messages
        return input.replaceAll("(?i)(key|token|secret|password)=[A-Za-z0-9_\\-\\.~]+", "$1=***");
    }
}
