package com.travel.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.recommendation.exception.GeminiApiException;
import com.travel.recommendation.exception.InvalidResponseException;
import com.travel.recommendation.model.DestinationRecommendationDto;
import com.travel.recommendation.model.TravelPreferencesRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GeminiServiceTest {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private GeminiService geminiService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        geminiService = new GeminiService(restTemplate, objectMapper);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void getRecommendations_WhenApiKeyMissing_ThrowsGeminiApiException() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "");
        TravelPreferencesRequest request = new TravelPreferencesRequest();
        request.setStartingCity("Mumbai");
        request.setBudgetAmount(30000.0);

        GeminiApiException exception = assertThrows(GeminiApiException.class, () ->
                geminiService.getRecommendationsAndEstimates(request)
        );
        assertEquals(401, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Gemini API key is missing"));
    }

    @Test
    void getRecommendations_WhenValidResponse_ParsesCorrectly() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-gemini-key");
        ReflectionTestUtils.setField(geminiService, "apiUrl", "https://generativelanguage.googleapis.com/v1beta/models");
        ReflectionTestUtils.setField(geminiService, "model", "gemini-1.5-flash");

        String geminiRawResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "```json\\n{\\"destinations\\": [{\\"name\\": \\"Udaipur\\", \\"state\\": \\"Rajasthan\\", \\"description\\": \\"City of lakes\\", \\"recommendationReasons\\": [\\"Heritage\\", \\"Scenic views\\"], \\"matchingInterests\\": [\\"Culture\\"], \\"suggestedActivities\\": [\\"Lake Pichola boat ride\\"], \\"itinerary\\": [{\\"day\\": 1, \\"title\\": \\"Arrival\\", \\"description\\": \\"Check in and relax\\", \\"activities\\": [\\"City Palace\\"], \\"estimatedCost\\": 1500.0}], \\"costBreakdown\\": {\\"roundTripTransportation\\": {\\"estimatedCost\\": 6000.0, \\"notes\\": \\"Train/Cab\\"}, \\"accommodation\\": {\\"estimatedCost\\": 8000.0, \\"notes\\": \\"Mid-range hotel\\"}, \\"food\\": {\\"estimatedCost\\": 4000.0, \\"notes\\": \\"Local eateries\\"}, \\"localTransportation\\": {\\"estimatedCost\\": 1500.0, \\"notes\\": \\"Auto-rickshaws\\"}, \\"activitiesAndEntryFees\\": {\\"estimatedCost\\": 2000.0, \\"notes\\": \\"Palace entry\\"}, \\"miscellaneous\\": {\\"estimatedCost\\": 1000.0, \\"notes\\": \\"Souvenirs\\"}, \\"totalTripCost\\": {\\"low\\": 20000.0, \\"typical\\": 22500.0, \\"high\\": 26000.0, \\"notes\\": \\"Based on standard travel\\"}, \\"perPersonCost\\": {\\"low\\": 10000.0, \\"typical\\": 11250.0, \\"high\\": 13000.0, \\"notes\\": \\"Per traveler\\"}}, \\"assumptionsAndUncertainties\\": [\\"Train fares subject to booking window\\"]}]}\\n```"
                      }
                    ]
                  }
                }
              ]
            }
            """;

        mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=test-gemini-key"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(geminiRawResponse, MediaType.APPLICATION_JSON));

        TravelPreferencesRequest request = new TravelPreferencesRequest();
        request.setStartingCity("Mumbai");
        request.setBudgetAmount(40000.0);
        request.setTravelersCount(2);
        request.setDurationDays(3);

        List<DestinationRecommendationDto> result = geminiService.getRecommendationsAndEstimates(request);
        mockServer.verify();

        assertNotNull(result);
        assertEquals(1, result.size());
        DestinationRecommendationDto dest = result.get(0);
        assertEquals("Udaipur", dest.getName());
        assertEquals("Rajasthan", dest.getState());
        assertEquals("City of lakes", dest.getDescription());
        assertNotNull(dest.getCostBreakdown());
        assertEquals(22500.0, dest.getCostBreakdown().getTotalTripCost().getTypical());
        assertEquals(11250.0, dest.getCostBreakdown().getPerPersonCost().getTypical());
        assertEquals(1, dest.getItinerary().size());
        assertEquals("Lake Pichola boat ride", dest.getSuggestedActivities().get(0));
    }

    @Test
    void getRecommendations_WhenGeminiFailsWith500_ThrowsGeminiApiException() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-key");
        ReflectionTestUtils.setField(geminiService, "apiUrl", "https://generativelanguage.googleapis.com/v1beta/models");
        ReflectionTestUtils.setField(geminiService, "model", "gemini-1.5-flash");

        mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=test-key"))
                .andRespond(withServerError());

        TravelPreferencesRequest request = new TravelPreferencesRequest();
        request.setStartingCity("Delhi");
        request.setBudgetAmount(20000.0);

        assertThrows(GeminiApiException.class, () -> geminiService.getRecommendationsAndEstimates(request));
        mockServer.verify();
    }

    @Test
    void parseAndValidate_WhenCandidateEmpty_ThrowsInvalidResponseException() {
        String invalidResponse = "{\"candidates\": []}";
        assertThrows(InvalidResponseException.class, () -> geminiService.parseAndValidateGeminiResponse(invalidResponse));
    }

    @Test
    void parseAndValidate_WhenMissingNameOrCost_ThrowsInvalidResponseException() {
        String invalidJson = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "{\\"destinations\\": [{\\"state\\": \\"Goa\\"}]}"
                      }
                    ]
                  }
                }
              ]
            }
            """;
        assertThrows(InvalidResponseException.class, () -> geminiService.parseAndValidateGeminiResponse(invalidJson));
    }
}
