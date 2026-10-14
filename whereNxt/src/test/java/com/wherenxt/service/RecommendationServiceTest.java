package com.wherenxt.service;

import com.wherenxt.config.GeminiProperties;
import com.wherenxt.dto.CostEstimatesDto;
import com.wherenxt.dto.CostRangeDto;
import com.wherenxt.dto.DestinationDto;
import com.wherenxt.dto.ImageMetadataDto;
import com.wherenxt.dto.RecommendationRequest;
import com.wherenxt.dto.RecommendationResponse;
import com.wherenxt.dto.gemini.GeminiDayPlan;
import com.wherenxt.dto.gemini.GeminiDestinationItem;
import com.wherenxt.dto.gemini.GeminiRecommendationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private WikimediaCommonsService wikimediaService;

    private GeminiProperties geminiProperties;
    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        geminiProperties = new GeminiProperties();
        geminiProperties.setKey("AIzaSyMockKey");
        recommendationService = new RecommendationService(geminiService, wikimediaService, geminiProperties);
    }

    @Test
    @DisplayName("getRecommendations orchestrates Gemini recommendations with Wikimedia Commons image attribution")
    void getRecommendations_orchestratesAiAndWikimedia() {
        RecommendationRequest request = new RecommendationRequest();
        request.setStartingCity("Bengaluru");
        request.setBudget(45000);
        request.setTravelers(2);
        request.setTripLengthDays(4);
        request.setInterests(Arrays.asList("Heritage", "Culinary"));

        GeminiDestinationItem geminiItem = new GeminiDestinationItem();
        geminiItem.setPlace("Jaipur");
        geminiItem.setState("Rajasthan");
        geminiItem.setMatch(96);
        geminiItem.setRecommendedDuration("4 days");
        geminiItem.setSummary("Historic palaces and artisan markets.");
        geminiItem.setTags(Arrays.asList("Heritage", "Culinary"));
        geminiItem.setActivities(Arrays.asList("City Palace", "Amber Fort"));
        geminiItem.setCostEstimates(new CostEstimatesDto(
                new CostRangeDto(4000, 5000, 7000),
                new CostRangeDto(7000, 10000, 14000),
                new CostRangeDto(4000, 6000, 8000),
                new CostRangeDto(1500, 2500, 3500),
                new CostRangeDto(2000, 3500, 5000),
                new CostRangeDto(1000, 1500, 2500),
                new CostRangeDto(19500, 28500, 40000)
        ));
        geminiItem.setDayByDayItinerary(Arrays.asList(
                new GeminiDayPlan("Day 1", "Old City", "City Palace tour"),
                new GeminiDayPlan("Day 2", "Forts", "Amber Fort sunset")
        ));

        GeminiRecommendationResult aiResult = new GeminiRecommendationResult();
        aiResult.setDestinations(List.of(geminiItem));
        when(geminiService.getRecommendations(any())).thenReturn(aiResult);

        ImageMetadataDto photo = new ImageMetadataDto(
                "https://upload.wikimedia.org/hawa_mahal.jpg",
                "https://upload.wikimedia.org/thumb_hawa_mahal.jpg",
                "File:Hawa Mahal.jpg",
                "Hawa Mahal Palace in Jaipur",
                "Jakub Halun",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Hawa_Mahal.jpg",
                "Photo: Jakub Halun via Wikimedia Commons, licensed under CC BY-SA 4.0"
        );
        when(wikimediaService.getBestDestinationImage(eq("Jaipur"), eq("Rajasthan"))).thenReturn(photo);

        RecommendationResponse response = recommendationService.getRecommendations(request);

        assertThat(response).isNotNull();
        assertThat(response.getStartingCity()).isEqualTo("Bengaluru");
        assertThat(response.getCurrency()).isEqualTo("INR");
        assertThat(response.getSource()).isEqualTo("GEMINI_LIVE");
        assertThat(response.getDestinations()).hasSize(1);

        DestinationDto dest = response.getDestinations().get(0);
        assertThat(dest.getPlace()).isEqualTo("Jaipur");
        assertThat(dest.getState()).isEqualTo("Rajasthan");
        assertThat(dest.getCostNum()).isEqualTo(28500);
        assertThat(dest.getCost()).contains("₹");

        // Verify Wikimedia Commons image integration
        assertThat(dest.getImage()).isEqualTo("https://upload.wikimedia.org/hawa_mahal.jpg");
        assertThat(dest.getCredit()).isEqualTo("Jakub Halun");
        assertThat(dest.getLicense()).isEqualTo("CC BY-SA 4.0");
        assertThat(dest.getSourceUrl()).isEqualTo("https://commons.wikimedia.org/wiki/File:Hawa_Mahal.jpg");

        // Verify cost percentage breakdown
        assertThat(dest.getCosts()).hasSize(4);
        int totalPct = dest.getCosts().stream().mapToInt(Integer::intValue).sum();
        assertThat(totalPct).isEqualTo(100);

        // Verify itinerary structure
        assertThat(dest.getItinerary()).hasSize(2);
        assertThat(dest.getItinerary().get(0)).containsExactly("Day 1", "Old City", "City Palace tour");
    }
}
