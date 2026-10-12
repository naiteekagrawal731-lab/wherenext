package com.travel.recommendation.service;

import com.travel.recommendation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TravelRecommendationServiceTest {

    private GeminiService geminiService;
    private GooglePlacesService googlePlacesService;
    private TravelRecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        geminiService = Mockito.mock(GeminiService.class);
        googlePlacesService = Mockito.mock(GooglePlacesService.class);
        recommendationService = new TravelRecommendationService(geminiService, googlePlacesService);
    }

    @Test
    void processRecommendations_CoordinatesServicesAndPreservesEstimates() {
        TravelPreferencesRequest request = new TravelPreferencesRequest();
        request.setStartingCity("Bengaluru");
        request.setBudgetType("TOTAL");
        request.setBudgetAmount(50000.0);
        request.setTravelersCount(2);
        request.setDurationDays(4);

        DestinationRecommendationDto dest = new DestinationRecommendationDto();
        dest.setName("Coorg");
        dest.setState("Karnataka");
        dest.setDescription("Scotland of India");

        CostBreakdownDto breakdown = new CostBreakdownDto();
        breakdown.setTotalTripCost(new CostRangeDto(28000.0, 32000.0, 38000.0, "Total for 2"));
        breakdown.setPerPersonCost(new CostRangeDto(14000.0, 16000.0, 19000.0, "Per person"));
        dest.setCostBreakdown(breakdown);

        when(geminiService.getRecommendationsAndEstimates(any())).thenReturn(Collections.singletonList(dest));

        PlaceDetailsDto placeDetails = new PlaceDetailsDto();
        placeDetails.setPlaceId("coorg-place-123");
        placeDetails.setRating(4.7);
        placeDetails.setPhotoAvailable(true);
        placeDetails.setPhotoUrl("/api/places/photo?name=coorg-photo");

        when(googlePlacesService.getPlaceDetailsForDestination("Coorg", "Karnataka")).thenReturn(placeDetails);

        RecommendationResponse response = recommendationService.processRecommendations(request);

        assertNotNull(response);
        assertEquals(request, response.getUserPreferences());
        assertEquals(1, response.getDestinations().size());

        DestinationRecommendationDto resultDest = response.getDestinations().get(0);
        assertEquals("Coorg", resultDest.getName());
        assertEquals(32000.0, resultDest.getCostBreakdown().getTotalTripCost().getTypical());
        assertNotNull(resultDest.getPlaceDetails());
        assertEquals("coorg-place-123", resultDest.getPlaceDetails().getPlaceId());
        assertTrue(resultDest.getPlaceDetails().isPhotoAvailable());
        assertNotNull(response.getDisclaimer());

        verify(geminiService, times(1)).getRecommendationsAndEstimates(request);
        verify(googlePlacesService, times(1)).getPlaceDetailsForDestination("Coorg", "Karnataka");
    }

    @Test
    void processRecommendations_WhenPlacesFails_HandlesGracefullyWithFallback() {
        TravelPreferencesRequest request = new TravelPreferencesRequest();
        request.setStartingCity("Delhi");
        request.setBudgetAmount(25000.0);

        DestinationRecommendationDto dest = new DestinationRecommendationDto();
        dest.setName("Manali");
        dest.setState("Himachal Pradesh");

        when(geminiService.getRecommendationsAndEstimates(any())).thenReturn(Collections.singletonList(dest));
        when(googlePlacesService.getPlaceDetailsForDestination(any(), any()))
                .thenThrow(new RuntimeException("Network timeout"));

        RecommendationResponse response = recommendationService.processRecommendations(request);

        assertNotNull(response);
        assertEquals(1, response.getDestinations().size());
        PlaceDetailsDto details = response.getDestinations().get(0).getPlaceDetails();
        assertNotNull(details);
        assertFalse(details.isPhotoAvailable());
        assertTrue(details.getPhotoUnavailableReason().contains("Unable to reach Google Places API"));
        assertTrue(details.getGoogleMapsUrl().contains("Manali"));
    }
}
