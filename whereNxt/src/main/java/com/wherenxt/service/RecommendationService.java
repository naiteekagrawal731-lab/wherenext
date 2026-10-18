package com.wherenxt.service;

import com.wherenxt.config.GeminiProperties;
import com.wherenxt.dto.CostEstimatesDto;
import com.wherenxt.dto.DestinationDto;
import com.wherenxt.dto.ImageMetadataDto;
import com.wherenxt.dto.RecommendationRequest;
import com.wherenxt.dto.RecommendationResponse;
import com.wherenxt.dto.gemini.GeminiDayPlan;
import com.wherenxt.dto.gemini.GeminiDestinationItem;
import com.wherenxt.dto.gemini.GeminiRecommendationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    private static final Locale INDIA_LOCALE = new Locale("en", "IN");

    private final GeminiService geminiService;
    private final WikimediaCommonsService wikimediaService;
    private final GeminiProperties geminiProperties;

    public RecommendationService(GeminiService geminiService,
                                 WikimediaCommonsService wikimediaService,
                                 GeminiProperties geminiProperties) {
        this.geminiService = geminiService;
        this.wikimediaService = wikimediaService;
        this.geminiProperties = geminiProperties;
    }

    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        log.info("Processing recommendations for city: {}, budget: ₹{}, travelers: {}, days: {}",
                request.getStartingCity(), request.getBudget(), request.getTravelers(), request.getTripLengthDays());

        // Step 1: Call Gemini Service to get structured AI travel recommendations
        GeminiRecommendationResult aiResult = geminiService.getRecommendations(request);

        // Step 2: Merge each destination with Wikimedia Commons images and format response
        List<DestinationDto> destinationDtos = new ArrayList<>();
        java.util.Set<String> usedImageUrls = new java.util.HashSet<>();
        long idCounter = 1;
        int travelers = Math.max(1, request.getTravelers() != null ? request.getTravelers() : 1);

        if (aiResult.getDestinations() != null) {
            for (GeminiDestinationItem item : aiResult.getDestinations()) {
                DestinationDto dto = new DestinationDto();
                dto.setId(idCounter++);
                dto.setPlace(item.getPlace());
                dto.setState(item.getState() != null ? item.getState() : "India");
                dto.setMatch(item.getMatch() > 0 ? item.getMatch() : 90);
                dto.setDuration(item.getRecommendedDuration() != null ? item.getRecommendedDuration() : request.getTripLengthDays() + " days");
                dto.setSummary(item.getSummary());
                dto.setTags(item.getTags() != null && !item.getTags().isEmpty() ? item.getTags() : request.getInterests());
                dto.setActivities(item.getActivities() != null ? item.getActivities() : new ArrayList<>());
                dto.setTravelTips(item.getTravelTips() != null ? item.getTravelTips() : new ArrayList<>());
                dto.setAssumptions(item.getAssumptions() != null ? item.getAssumptions() : new ArrayList<>());
                dto.setUncertaintyMarkers(item.getUncertaintyMarkers() != null ? item.getUncertaintyMarkers() : new ArrayList<>());

                // Ensure cost estimates are complete including perPerson
                CostEstimatesDto costEstimates = item.getCostEstimates();
                if (costEstimates != null) {
                    if (costEstimates.getPerPerson() == null && costEstimates.getTotal() != null) {
                        int pLow = costEstimates.getTotal().getLow() / travelers;
                        int pTyp = costEstimates.getTotal().getTypical() / travelers;
                        int pHigh = costEstimates.getTotal().getHigh() / travelers;
                        costEstimates.setPerPerson(new com.wherenxt.dto.CostRangeDto(pLow, pTyp, pHigh));
                    }
                }
                dto.setCostEstimates(costEstimates);

                // Calculate cost numbers and format as INR
                int totalCost = calculateTotalCost(costEstimates, request.getBudget());
                dto.setCostNum(totalCost);
                dto.setCost(formatInr(totalCost));

                // Calculate 4 percentage breakdown segments: [transport, food, stay, activities]
                dto.setCosts(calculateCostPercentages(costEstimates));

                // Format itinerary into list of [day, title, detail]
                dto.setItinerary(formatItinerary(item.getDayByDayItinerary(), request.getTripLengthDays()));

                // Step 3: Fetch matching photograph and attribution from Wikimedia Commons API
                ImageMetadataDto photo = wikimediaService.getBestDestinationImage(item.getPlace(), item.getState(), item.getActivities(), usedImageUrls);
                String chosenUrl = (photo.getOriginalUrl() != null && !photo.getOriginalUrl().trim().isEmpty())
                        ? photo.getOriginalUrl()
                        : photo.getThumbnailUrl();
                dto.setImage(WikimediaCommonsService.cleanWikimediaImageUrl(chosenUrl));
                if (photo.getOriginalUrl() != null) usedImageUrls.add(photo.getOriginalUrl());
                if (photo.getThumbnailUrl() != null) usedImageUrls.add(photo.getThumbnailUrl());
                if (chosenUrl != null) usedImageUrls.add(chosenUrl);

                dto.setCredit(photo.getArtist());
                dto.setHandle(photo.getLicense());
                dto.setSourceUrl(photo.getSourcePageUrl());
                dto.setLicense(photo.getLicense());
                dto.setLicenseUrl(photo.getLicenseUrl());
                dto.setImageMetadata(photo);

                destinationDtos.add(dto);
            }
        }

        RecommendationResponse response = new RecommendationResponse();
        response.setStartingCity(request.getStartingCity());
        response.setTripLengthDays(request.getTripLengthDays());
        response.setTravelers(travelers);
        response.setTotalBudget(request.getBudget());
        response.setBudgetPerPerson(request.isGroupBudget() ? request.getBudget() / travelers : request.getBudget());
        response.setCurrency("INR");
        response.setGeneratedAt(Instant.now().toString());
        response.setFallback(aiResult.isFallback());
        response.setFallbackReason(aiResult.getFallbackReason());
        response.setSource(aiResult.isFallback() ? "CURATED_FALLBACK" : "GEMINI_LIVE");
        response.setDestinations(destinationDtos);

        return response;
    }

    private int calculateTotalCost(CostEstimatesDto estimates, int budgetFallback) {
        if (estimates != null && estimates.getTotal() != null && estimates.getTotal().getTypical() > 0) {
            return estimates.getTotal().getTypical();
        }
        return (int) (budgetFallback * 0.9);
    }

    private String formatInr(int amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(INDIA_LOCALE);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(amount).replace("INR", "₹").trim();
    }

    private List<Integer> calculateCostPercentages(CostEstimatesDto estimates) {
        if (estimates != null &&
                estimates.getTransport() != null &&
                estimates.getFood() != null &&
                estimates.getAccommodation() != null &&
                estimates.getActivities() != null) {

            double transport = estimates.getTransport().getTypical();
            double food = estimates.getFood().getTypical();
            double stay = estimates.getAccommodation().getTypical();
            double act = estimates.getActivities().getTypical();
            double sum = transport + food + stay + act;

            if (sum > 0) {
                int pTransport = (int) Math.round((transport / sum) * 100);
                int pFood = (int) Math.round((food / sum) * 100);
                int pStay = (int) Math.round((stay / sum) * 100);
                int pAct = 100 - (pTransport + pFood + pStay);
                return Arrays.asList(pTransport, pFood, pStay, Math.max(5, pAct));
            }
        }
        // Default balanced breakdown: [22% travel, 28% food, 32% stay, 18% activities]
        return Arrays.asList(22, 28, 32, 18);
    }

    private List<List<String>> formatItinerary(List<GeminiDayPlan> plans, int targetDays) {
        List<List<String>> list = new ArrayList<>();
        if (plans != null && !plans.isEmpty()) {
            for (GeminiDayPlan plan : plans) {
                list.add(Arrays.asList(
                        plan.getDay() != null ? plan.getDay() : "Day " + (list.size() + 1),
                        plan.getTitle() != null ? plan.getTitle() : "Local Discovery",
                        plan.getDetail() != null ? plan.getDetail() : "Explore historical and cultural sights."
                ));
            }
        } else {
            for (int i = 1; i <= Math.min(targetDays, 3); i++) {
                list.add(Arrays.asList(
                        "Day " + i,
                        "Explore local highlights",
                        "Immerse yourself in authentic regional culture and flavors."
                ));
            }
        }
        return list;
    }
}
