package com.travel.recommendation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.recommendation.exception.GeminiApiException;
import com.travel.recommendation.model.*;
import com.travel.recommendation.service.GooglePlacesService;
import com.travel.recommendation.service.TravelRecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TravelRecommendationController.class)
class TravelRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TravelRecommendationService travelRecommendationService;

    @MockBean
    private GooglePlacesService googlePlacesService;

    @Test
    void getRecommendations_WhenValidRequest_ReturnsOkWithData() throws Exception {
        TravelPreferencesRequest request = new TravelPreferencesRequest();
        request.setStartingCity("Mumbai");
        request.setBudgetType("TOTAL");
        request.setBudgetAmount(50000.0);
        request.setTravelersCount(2);
        request.setDurationDays(4);

        DestinationRecommendationDto dest = new DestinationRecommendationDto();
        dest.setName("Goa");
        dest.setState("Goa");
        dest.setDescription("Sunny beach state");

        CostBreakdownDto breakdown = new CostBreakdownDto();
        breakdown.setTotalTripCost(new CostRangeDto(30000.0, 35000.0, 42000.0, "Total estimate"));
        breakdown.setPerPersonCost(new CostRangeDto(15000.0, 17500.0, 21000.0, "Per person estimate"));
        dest.setCostBreakdown(breakdown);

        RecommendationResponse response = new RecommendationResponse(request, Collections.singletonList(dest));
        when(travelRecommendationService.processRecommendations(any())).thenReturn(response);

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinations", hasSize(1)))
                .andExpect(jsonPath("$.destinations[0].name").value("Goa"))
                .andExpect(jsonPath("$.destinations[0].costBreakdown.totalTripCost.typical").value(35000.0))
                .andExpect(jsonPath("$.disclaimer").isNotEmpty());
    }

    @Test
    void getRecommendations_WhenInvalidInput_ReturnsBadRequestWithValidationErrors() throws Exception {
        TravelPreferencesRequest invalidRequest = new TravelPreferencesRequest();
        invalidRequest.setStartingCity(""); // Invalid: blank
        invalidRequest.setBudgetAmount(-500.0); // Invalid: negative
        invalidRequest.setDurationDays(0); // Invalid: < 1

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void getRecommendations_WhenGeminiFails_ReturnsBadGateway() throws Exception {
        TravelPreferencesRequest request = new TravelPreferencesRequest();
        request.setStartingCity("Delhi");
        request.setBudgetAmount(30000.0);

        when(travelRecommendationService.processRecommendations(any()))
                .thenThrow(new GeminiApiException("Gemini quota exceeded", 502));

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("GEMINI_API_ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("Gemini quota exceeded")));
    }

    @Test
    void getPlacePhoto_WhenValid_ReturnsImageBytesAndCacheHeaders() throws Exception {
        byte[] fakePhoto = new byte[]{1, 2, 3, 4};
        when(googlePlacesService.fetchPhotoBytes("places/xyz/photos/123", null, 800, 600)).thenReturn(fakePhoto);

        mockMvc.perform(get("/api/places/photo")
                        .param("name", "places/xyz/photos/123")
                        .param("maxWidth", "800")
                        .param("maxHeight", "600"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_JPEG_VALUE))
                .andExpect(header().string("Cache-Control", containsString("max-age=604800")))
                .andExpect(content().bytes(fakePhoto));
    }

    @Test
    void getApiStatus_ReturnsStatusMap() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationStatus").value("UP"));
    }
}
