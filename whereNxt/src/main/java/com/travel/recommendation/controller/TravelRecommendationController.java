package com.travel.recommendation.controller;

import com.travel.recommendation.model.RecommendationResponse;
import com.travel.recommendation.model.TravelPreferencesRequest;
import com.travel.recommendation.service.GooglePlacesService;
import com.travel.recommendation.service.TravelRecommendationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TravelRecommendationController {

    private static final Logger log = LoggerFactory.getLogger(TravelRecommendationController.class);

    private final TravelRecommendationService travelRecommendationService;
    private final GooglePlacesService googlePlacesService;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${google.places.api.key:}")
    private String placesApiKey;

    public TravelRecommendationController(TravelRecommendationService travelRecommendationService,
                                          GooglePlacesService googlePlacesService) {
        this.travelRecommendationService = travelRecommendationService;
        this.googlePlacesService = googlePlacesService;
    }

    @PostMapping("/recommendations")
    public ResponseEntity<RecommendationResponse> getRecommendations(@Valid @RequestBody TravelPreferencesRequest request) {
        log.info("Received recommendation request for starting city: {}", request.getStartingCity());
        RecommendationResponse response = travelRecommendationService.processRecommendations(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/places/photo")
    public ResponseEntity<byte[]> getPlacePhoto(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String photoReference,
            @RequestParam(required = false) Integer maxWidth,
            @RequestParam(required = false) Integer maxHeight) {

        byte[] photoBytes = googlePlacesService.fetchPhotoBytes(name, photoReference, maxWidth, maxHeight);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(photoBytes);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getApiStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("geminiConfigured", geminiApiKey != null && !geminiApiKey.trim().isEmpty());
        status.put("placesConfigured", placesApiKey != null && !placesApiKey.trim().isEmpty());
        status.put("applicationStatus", "UP");
        return ResponseEntity.ok(status);
    }
}
