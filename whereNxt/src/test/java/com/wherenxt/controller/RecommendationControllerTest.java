package com.wherenxt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wherenxt.dto.CostEstimatesDto;
import com.wherenxt.dto.CostRangeDto;
import com.wherenxt.dto.DestinationDto;
import com.wherenxt.dto.ImageMetadataDto;
import com.wherenxt.dto.RecommendationRequest;
import com.wherenxt.dto.RecommendationResponse;
import com.wherenxt.service.RecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecommendationService recommendationService;

    @Test
    @DisplayName("POST /api/recommendations with valid request returns 200 OK and unified payload")
    void getRecommendations_validRequest_returnsOk() throws Exception {
        RecommendationRequest request = new RecommendationRequest();
        request.setStartingCity("Mumbai");
        request.setBudget(50000);
        request.setGroupBudget(true);
        request.setTravelers(2);
        request.setTripLengthDays(4);
        request.setInterests(Arrays.asList("Heritage", "Culinary"));
        request.setPace("Balanced");
        request.setTransport("Train + local");
        request.setStayStyle("Boutique hotels");
        request.setCompanion("Partner");
        request.setNotes("Vegetarian");

        RecommendationResponse mockResponse = new RecommendationResponse();
        mockResponse.setStartingCity("Mumbai");
        mockResponse.setTripLengthDays(4);
        mockResponse.setTravelers(2);
        mockResponse.setTotalBudget(50000);
        mockResponse.setBudgetPerPerson(25000);
        mockResponse.setCurrency("INR");
        mockResponse.setGeneratedAt(Instant.now().toString());
        mockResponse.setSource("GEMINI_LIVE");

        DestinationDto dest = new DestinationDto();
        dest.setId(1);
        dest.setPlace("Jaipur");
        dest.setState("Rajasthan");
        dest.setMatch(95);
        dest.setDuration("4 days");
        dest.setCost("₹28,000");
        dest.setCostNum(28000);
        dest.setImage("https://upload.wikimedia.org/wikipedia/commons/hawa_mahal.jpg");
        dest.setCredit("Jakub Halun");
        dest.setHandle("CC BY-SA 4.0");
        dest.setSourceUrl("https://commons.wikimedia.org/wiki/File:Hawa_Mahal.jpg");
        dest.setLicense("CC BY-SA 4.0");
        dest.setTags(Arrays.asList("Heritage", "Culinary"));
        dest.setSummary("Historic pink city havelis and culinary delights.");
        dest.setCosts(Arrays.asList(22, 28, 32, 18));
        dest.setItinerary(List.of(
                Arrays.asList("Day 1", "Royal Arrival", "City Palace tour"),
                Arrays.asList("Day 2", "Forts & Views", "Amber Fort visit")
        ));
        dest.setCostEstimates(new CostEstimatesDto(
                new CostRangeDto(4000, 6000, 9000),
                new CostRangeDto(8000, 12000, 18000),
                new CostRangeDto(4000, 6000, 9000),
                new CostRangeDto(1500, 2500, 4000),
                new CostRangeDto(2000, 3500, 5000),
                new CostRangeDto(1000, 2000, 3000),
                new CostRangeDto(20500, 32000, 48000)
        ));
        dest.setTravelTips(Arrays.asList("Book palace entry online"));

        mockResponse.setDestinations(List.of(dest));

        when(recommendationService.getRecommendations(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startingCity").value("Mumbai"))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.destinations[0].place").value("Jaipur"))
                .andExpect(jsonPath("$.destinations[0].state").value("Rajasthan"))
                .andExpect(jsonPath("$.destinations[0].cost").value("₹28,000"))
                .andExpect(jsonPath("$.destinations[0].credit").value("Jakub Halun"))
                .andExpect(jsonPath("$.destinations[0].license").value("CC BY-SA 4.0"))
                .andExpect(jsonPath("$.destinations[0].costs[0]").value(22))
                .andExpect(jsonPath("$.destinations[0].costEstimates.transport.typical").value(6000));
    }

    @Test
    @DisplayName("POST /api/recommendations with budget under ₹5,000 returns 400 Bad Request")
    void getRecommendations_budgetTooLow_returnsBadRequest() throws Exception {
        RecommendationRequest request = new RecommendationRequest();
        request.setStartingCity("Delhi");
        request.setBudget(3000); // Below 5000 min
        request.setInterests(List.of("Heritage"));

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("POST /api/recommendations with blank starting city returns 400 Bad Request")
    void getRecommendations_blankCity_returnsBadRequest() throws Exception {
        RecommendationRequest request = new RecommendationRequest();
        request.setStartingCity("");
        request.setBudget(25000);
        request.setInterests(List.of("Adventure"));

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    @DisplayName("POST /api/recommendations with empty interests list returns 400 Bad Request")
    void getRecommendations_emptyInterests_returnsBadRequest() throws Exception {
        RecommendationRequest request = new RecommendationRequest();
        request.setStartingCity("Chennai");
        request.setBudget(35000);
        request.setInterests(Collections.emptyList());

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }
}
