package com.wherenxt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wherenxt.config.GeminiProperties;
import com.wherenxt.dto.CostEstimatesDto;
import com.wherenxt.dto.RecommendationRequest;
import com.wherenxt.dto.gemini.GeminiGenerateResponse;
import com.wherenxt.dto.gemini.GeminiRecommendationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private GeminiProperties properties;
    private ObjectMapper objectMapper;
    private GeminiService service;

    @BeforeEach
    void setUp() {
        properties = new GeminiProperties();
        objectMapper = new ObjectMapper();
        service = new GeminiService(restTemplate, properties, objectMapper);
    }

    @Test
    @DisplayName("getRecommendations when key is configured calls Gemini and parses JSON with code fences")
    void getRecommendations_withKeyConfigured_parsesJsonResponse() {
        properties.setKey("AIzaSyTestApiKey12345");
        properties.setModel("gemini-1.5-flash");

        String mockGeminiJson = """
                ```json
                {
                  "destinations": [
                    {
                      "place": "Udaipur",
                      "state": "Rajasthan",
                      "match": 95,
                      "recommendedDuration": "4 days",
                      "summary": "City of lakes and royal palaces.",
                      "tags": ["Heritage", "Culinary"],
                      "activities": ["Lake Pichola boat ride", "City Palace tour"],
                      "travelTips": ["Pre-book boat rides during sunset"],
                      "costEstimates": {
                        "transport": {"low": 3000, "typical": 5000, "high": 8000, "currency": "INR"},
                        "accommodation": {"low": 6000, "typical": 10000, "high": 16000, "currency": "INR"},
                        "food": {"low": 3000, "typical": 5000, "high": 8000, "currency": "INR"},
                        "localTravel": {"low": 1500, "typical": 2500, "high": 4000, "currency": "INR"},
                        "activities": {"low": 2000, "typical": 3500, "high": 6000, "currency": "INR"},
                        "miscellaneous": {"low": 1000, "typical": 2000, "high": 3500, "currency": "INR"},
                        "total": {"low": 16500, "typical": 28000, "high": 45500, "currency": "INR"}
                      },
                      "dayByDayItinerary": [
                        {"day": "Day 1", "title": "Arrival", "detail": "Lake Pichola arrival"}
                      ],
                      "assumptions": ["Boutique hotel stay"],
                      "uncertaintyMarkers": ["Seasonal price fluctuations"]
                    }
                  ]
                }
                ```
                """;

        GeminiGenerateResponse geminiResponse = new GeminiGenerateResponse();
        GeminiGenerateResponse.Candidate candidate = new GeminiGenerateResponse.Candidate();
        GeminiGenerateResponse.Content content = new GeminiGenerateResponse.Content();
        content.setParts(List.of(new GeminiGenerateResponse.Part(mockGeminiJson)));
        candidate.setContent(content);
        geminiResponse.setCandidates(List.of(candidate));

        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(GeminiGenerateResponse.class)))
                .thenReturn(ResponseEntity.ok(geminiResponse));

        RecommendationRequest request = new RecommendationRequest();
        request.setStartingCity("Bengaluru");
        request.setBudget(45000);
        request.setTravelers(2);
        request.setTripLengthDays(4);
        request.setInterests(Arrays.asList("Heritage", "Culinary"));

        GeminiRecommendationResult result = service.getRecommendations(request);

        assertThat(result).isNotNull();
        assertThat(result.getDestinations()).hasSize(1);
        assertThat(result.getDestinations().get(0).getPlace()).isEqualTo("Udaipur");
        assertThat(result.getDestinations().get(0).getState()).isEqualTo("Rajasthan");
        assertThat(result.getDestinations().get(0).getMatch()).isEqualTo(95);
        assertThat(result.getDestinations().get(0).getCostEstimates().getTotal().getTypical()).isEqualTo(28000);
    }

    @Test
    @DisplayName("getRecommendations when key is not configured returns rich curated Indian destinations")
    void getRecommendations_withoutKey_returnsCuratedFallback() {
        properties.setKey(""); // Empty key

        RecommendationRequest request = new RecommendationRequest();
        request.setStartingCity("Delhi");
        request.setBudget(40000);
        request.setTravelers(2);
        request.setTripLengthDays(5);
        request.setInterests(Arrays.asList("Heritage", "Culinary"));

        GeminiRecommendationResult result = service.getRecommendations(request);

        assertThat(result).isNotNull();
        assertThat(result.getDestinations()).hasSizeGreaterThanOrEqualTo(3);

        // Verify cost ranges in INR across all categories
        CostEstimatesDto estimates = result.getDestinations().get(0).getCostEstimates();
        assertThat(estimates).isNotNull();
        assertThat(estimates.getTransport().getLow()).isGreaterThan(0);
        assertThat(estimates.getTransport().getTypical()).isGreaterThan(estimates.getTransport().getLow());
        assertThat(estimates.getTransport().getHigh()).isGreaterThan(estimates.getTransport().getTypical());
        assertThat(estimates.getTransport().getCurrency()).isEqualTo("INR");

        assertThat(estimates.getTotal().getLow()).isGreaterThan(0);
        assertThat(estimates.getTotal().getTypical()).isGreaterThan(estimates.getTotal().getLow());
        assertThat(estimates.getTotal().getHigh()).isGreaterThan(estimates.getTotal().getTypical());
    }

    @Test
    @DisplayName("getRecommendations gracefully falls back when Gemini API throws exception")
    void getRecommendations_whenApiThrowsException_fallsBackGracefully() {
        properties.setKey("AIzaSySomeValidFormatKey");

        when(restTemplate.postForEntity(any(URI.class), any(HttpEntity.class), eq(GeminiGenerateResponse.class)))
                .thenThrow(new RuntimeException("Rate limit exceeded"));

        RecommendationRequest request = new RecommendationRequest();
        request.setStartingCity("Hyderabad");
        request.setBudget(35000);
        request.setTravelers(1);
        request.setTripLengthDays(3);
        request.setInterests(List.of("Heritage"));

        GeminiRecommendationResult result = service.getRecommendations(request);

        assertThat(result).isNotNull();
        assertThat(result.getDestinations()).isNotEmpty();
        assertThat(result.getDestinations().get(0).getPlace()).isNotEmpty();
    }
}
