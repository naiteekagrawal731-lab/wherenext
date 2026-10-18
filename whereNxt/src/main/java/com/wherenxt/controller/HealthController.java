package com.wherenxt.controller;

import com.wherenxt.config.GeminiProperties;
import com.wherenxt.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final GeminiProperties geminiProperties;

    public HealthController(GeminiProperties geminiProperties) {
        this.geminiProperties = geminiProperties;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> healthCheck() {
        HealthResponse response = new HealthResponse(
                "UP",
                "whereNxt-backend",
                Instant.now().toString(),
                geminiProperties.isKeyConfigured(),
                "1.0.0"
        );
        return ResponseEntity.ok(response);
    }
}
