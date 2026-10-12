package com.travel.recommendation.service;

import com.travel.recommendation.model.DestinationRecommendationDto;
import com.travel.recommendation.model.PlaceDetailsDto;
import com.travel.recommendation.model.RecommendationResponse;
import com.travel.recommendation.model.TravelPreferencesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TravelRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(TravelRecommendationService.class);

    private final GeminiService geminiService;
    private final GooglePlacesService googlePlacesService;

    public TravelRecommendationService(GeminiService geminiService, GooglePlacesService googlePlacesService) {
        this.geminiService = geminiService;
        this.googlePlacesService = googlePlacesService;
    }

    public RecommendationResponse processRecommendations(TravelPreferencesRequest preferences) {
        log.info("Processing travel recommendations for starting city: {}, travelers: {}, duration: {} days",
                preferences.getStartingCity(), preferences.getTravelersCount(), preferences.getDurationDays());

        // 1. Get recommendations and cost estimates directly from Gemini
        List<DestinationRecommendationDto> destinations = geminiService.getRecommendationsAndEstimates(preferences);

        // 2. Enrich each destination with real Google Places details & photos
        for (DestinationRecommendationDto destination : destinations) {
            try {
                PlaceDetailsDto placeDetails = googlePlacesService.getPlaceDetailsForDestination(
                        destination.getName(), destination.getState()
                );
                destination.setPlaceDetails(placeDetails);
            } catch (Exception ex) {
                log.warn("Failed to retrieve Google Places details for '{}': {}", destination.getName(), ex.getMessage());
                PlaceDetailsDto fallback = new PlaceDetailsDto();
                fallback.setPhotoAvailable(false);
                fallback.setPhotoUnavailableReason("Unable to reach Google Places API: " + ex.getMessage());
                fallback.setGoogleMapsUrl("https://www.google.com/maps/search/?api=1&query=" + destination.getName() + ", India");
                destination.setPlaceDetails(fallback);
            }
        }

        // 3. Assemble response without altering Gemini's estimates or auto-rejecting
        RecommendationResponse response = new RecommendationResponse(preferences, destinations);
        log.info("Successfully assembled {} destination recommendations", destinations.size());
        return response;
    }
}
