package com.travel.recommendation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.recommendation.exception.GeminiApiException;
import com.travel.recommendation.exception.InvalidResponseException;
import com.travel.recommendation.model.DestinationRecommendationDto;
import com.travel.recommendation.model.TravelPreferencesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-1.5-flash}")
    private String model;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiUrl;

    public GeminiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<DestinationRecommendationDto> getRecommendationsAndEstimates(TravelPreferencesRequest preferences) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new GeminiApiException(
                    "Gemini API key is missing. Please set the GEMINI_API_KEY environment variable or 'gemini.api.key' in application.properties.",
                    HttpStatus.UNAUTHORIZED.value()
            );
        }

        String prompt = buildPrompt(preferences);
        String requestUrl = apiUrl + "/" + model + ":generateContent?key=" + apiKey.trim();

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");
        content.put("parts", Collections.singletonList(part));

        requestBody.put("contents", Collections.singletonList(content));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.3);
        generationConfig.put("responseMimeType", "application/json");
        requestBody.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String responseBody;
        try {
            log.info("Sending travel preferences to Gemini API (model: {})", model);
            ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.POST, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new GeminiApiException("Gemini API returned empty or non-successful status: " + response.getStatusCode());
            }
            responseBody = response.getBody();
        } catch (HttpStatusCodeException ex) {
            log.error("Gemini API HTTP Error {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new GeminiApiException(
                    "Gemini API error (" + ex.getStatusCode() + "): " + ex.getStatusText() + ". Details: " + ex.getResponseBodyAsString(),
                    ex.getStatusCode().value()
            );
        } catch (Exception ex) {
            log.error("Error communicating with Gemini API", ex);
            throw new GeminiApiException("Failed to communicate with Gemini API: " + ex.getMessage(), ex);
        }

        return parseAndValidateGeminiResponse(responseBody);
    }

    private String buildPrompt(TravelPreferencesRequest prefs) {
        return """
            You are an expert Indian travel planning and pricing assistant.
            The user wants travel destination recommendations and detailed cost estimates based on their specific preferences:
            
            USER TRAVEL PREFERENCES:
            - Starting City: %s
            - Budget Type: %s
            - Budget Amount: ₹%s (INR)
            - Number of Travelers: %d
            - Trip Duration: %d days
            - Travel Dates: %s to %s
            - Interests: %s
            - Travel Style: %s
            - Preferred Transportation: %s
            - Accommodation Preference: %s
            - Travel Companions: %s
            - Travel Pace: %s
            
            REQUIREMENTS:
            1. Recommend 3 to 4 distinct travel destinations in India that best match these preferences and travel style.
            2. For each destination, provide:
               - name: Destination city or town (e.g., 'Jaipur', 'Munnar', 'Varanasi', 'Goa')
               - state: Indian state (e.g., 'Rajasthan', 'Kerala', 'Uttar Pradesh')
               - description: An engaging overview of the destination and why it is great for this traveler.
               - recommendationReasons: Array of strings highlighting reasons this destination matches their requirements.
               - matchingInterests: Array of strings of user's interests satisfied by this place.
               - suggestedActivities: Array of strings of top experiences and attractions.
               - itinerary: Array of daily plans with day (integer 1..duration), title, description, activities (array of strings), estimatedCost (number in INR for that day).
               - costBreakdown: Detailed destination-specific estimates in Indian Rupees (INR) accounting for group size (%d travelers), duration (%d days), starting city (%s), transportation style, and accommodation preference:
                 * roundTripTransportation: { estimatedCost: number, notes: string } (e.g. flights/trains/cabs from %s)
                 * accommodation: { estimatedCost: number, notes: string }
                 * food: { estimatedCost: number, notes: string }
                 * localTransportation: { estimatedCost: number, notes: string }
                 * activitiesAndEntryFees: { estimatedCost: number, notes: string }
                 * miscellaneous: { estimatedCost: number, notes: string }
                 * totalTripCost: { low: number, typical: number, high: number, notes: string }
                 * perPersonCost: { low: number, typical: number, high: number, notes: string }
               - assumptionsAndUncertainties: Array of strings explaining key cost assumptions (e.g. season pricing, booking lead time, travel class) or noting where reliable estimates are unavailable rather than inventing false precision.
            
            CRITICAL GUIDELINES:
            - All monetary figures MUST be in Indian Rupees (INR).
            - Provide reasonable destination-specific estimates.
            - If reliable estimates are unavailable for any component, state that clearly in notes and assumptions rather than inventing arbitrary precise figures.
            - Output MUST be valid JSON matching the exact schema:
            {
              "destinations": [
                {
                  "name": "...",
                  "state": "...",
                  "description": "...",
                  "recommendationReasons": ["..."],
                  "matchingInterests": ["..."],
                  "suggestedActivities": ["..."],
                  "itinerary": [
                    { "day": 1, "title": "...", "description": "...", "activities": ["..."], "estimatedCost": 1200.0 }
                  ],
                  "costBreakdown": {
                    "roundTripTransportation": { "estimatedCost": 5000.0, "notes": "..." },
                    "accommodation": { "estimatedCost": 8000.0, "notes": "..." },
                    "food": { "estimatedCost": 4500.0, "notes": "..." },
                    "localTransportation": { "estimatedCost": 2000.0, "notes": "..." },
                    "activitiesAndEntryFees": { "estimatedCost": 1500.0, "notes": "..." },
                    "miscellaneous": { "estimatedCost": 1000.0, "notes": "..." },
                    "totalTripCost": { "low": 19000.0, "typical": 22000.0, "high": 27000.0, "notes": "..." },
                    "perPersonCost": { "low": 9500.0, "typical": 11000.0, "high": 13500.0, "notes": "..." }
                  },
                  "assumptionsAndUncertainties": ["..."]
                }
              ]
            }
            Do not output any markdown or commentary outside of the JSON.
            """.formatted(
                prefs.getStartingCity(),
                prefs.getBudgetType(),
                prefs.getBudgetAmount() != null ? String.format("%.2f", prefs.getBudgetAmount()) : "Flexible",
                prefs.getTravelersCount(),
                prefs.getDurationDays(),
                prefs.getStartDate() != null ? prefs.getStartDate() : "Upcoming flexible",
                prefs.getEndDate() != null ? prefs.getEndDate() : "Flexible",
                prefs.getInterests() != null && !prefs.getInterests().isEmpty() ? String.join(", ", prefs.getInterests()) : "General sightseeing",
                prefs.getTravelStyle(),
                prefs.getPreferredTransportation(),
                prefs.getAccommodationPreference(),
                prefs.getCompanions(),
                prefs.getTravelPace(),
                prefs.getTravelersCount(),
                prefs.getDurationDays(),
                prefs.getStartingCity(),
                prefs.getStartingCity()
        );
    }

    public List<DestinationRecommendationDto> parseAndValidateGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
                throw new InvalidResponseException("Gemini returned no candidates in response");
            }

            JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
            if (textNode.isMissingNode() || textNode.asText().trim().isEmpty()) {
                throw new InvalidResponseException("Gemini candidate content contains no text");
            }

            String rawText = textNode.asText().trim();
            String cleanedJson = extractJson(rawText);

            JsonNode parsedJson = objectMapper.readTree(cleanedJson);
            List<DestinationRecommendationDto> destinations;

            if (parsedJson.isArray()) {
                destinations = objectMapper.convertValue(parsedJson, new TypeReference<List<DestinationRecommendationDto>>() {});
            } else if (parsedJson.has("destinations") && parsedJson.get("destinations").isArray()) {
                destinations = objectMapper.convertValue(parsedJson.get("destinations"), new TypeReference<List<DestinationRecommendationDto>>() {});
            } else {
                throw new InvalidResponseException("Gemini response JSON does not contain a 'destinations' array");
            }

            if (destinations == null || destinations.isEmpty()) {
                throw new InvalidResponseException("Gemini returned zero destination recommendations");
            }

            for (DestinationRecommendationDto dest : destinations) {
                if (dest.getName() == null || dest.getName().trim().isEmpty()) {
                    throw new InvalidResponseException("A recommended destination is missing a name");
                }
                if (dest.getCostBreakdown() == null) {
                    throw new InvalidResponseException("Destination '" + dest.getName() + "' is missing cost breakdown");
                }
            }

            return destinations;
        } catch (InvalidResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to parse Gemini response: {}", ex.getMessage(), ex);
            throw new InvalidResponseException("Failed to parse Gemini response into valid recommendations: " + ex.getMessage(), ex);
        }
    }

    private String extractJson(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        trimmed = trimmed.trim();

        int firstBrace = trimmed.indexOf('{');
        int firstBracket = trimmed.indexOf('[');

        int start = -1;
        if (firstBrace != -1 && firstBracket != -1) {
            start = Math.min(firstBrace, firstBracket);
        } else if (firstBrace != -1) {
            start = firstBrace;
        } else if (firstBracket != -1) {
            start = firstBracket;
        }

        if (start != -1) {
            int lastBrace = trimmed.lastIndexOf('}');
            int lastBracket = trimmed.lastIndexOf(']');
            int end = Math.max(lastBrace, lastBracket);
            if (end > start) {
                trimmed = trimmed.substring(start, end + 1);
            }
        }

        return trimmed;
    }
}
