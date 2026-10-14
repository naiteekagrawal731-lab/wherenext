package com.wherenxt.controller;

import com.wherenxt.dto.RecommendationRequest;
import com.wherenxt.dto.RecommendationResponse;
import com.wherenxt.service.RecommendationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RecommendationController {

    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/recommendations")
    public ResponseEntity<RecommendationResponse> getRecommendations(@Valid @RequestBody RecommendationRequest request) {
        log.info("Received recommendation request for city: {}", request.getStartingCity());
        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(response);
    }
}
