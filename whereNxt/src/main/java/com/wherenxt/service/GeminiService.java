package com.wherenxt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wherenxt.config.GeminiProperties;
import com.wherenxt.dto.CostEstimatesDto;
import com.wherenxt.dto.CostRangeDto;
import com.wherenxt.dto.RecommendationRequest;
import com.wherenxt.dto.gemini.GeminiDayPlan;
import com.wherenxt.dto.gemini.GeminiDestinationItem;
import com.wherenxt.dto.gemini.GeminiGenerateRequest;
import com.wherenxt.dto.gemini.GeminiGenerateResponse;
import com.wherenxt.dto.gemini.GeminiRecommendationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final RestTemplate restTemplate;
    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;

    public GeminiService(RestTemplate restTemplate, GeminiProperties properties, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * Generate destination recommendations using Gemini API with structured prompt,
     * bounded retry with exponential backoff & jitter for transient errors,
     * fail-fast on permanent client errors, and explicit fallback identification.
     */
    public GeminiRecommendationResult getRecommendations(RecommendationRequest request) {
        if (!properties.isKeyConfigured()) {
            log.info("GEMINI_API_KEY not configured. Providing high-quality curated travel recommendations.");
            return generateCuratedFallback(request, "Gemini API key not configured in environment (GEMINI_API_KEY). Using curated intelligence.");
        }

        log.info("Calling Gemini API [{}] using key [{}] for starting city: {}",
                properties.getModel(), properties.getMaskedKey(), request.getStartingCity());

        String prompt = buildPrompt(request);
        GeminiGenerateRequest geminiRequest = GeminiGenerateRequest.of(prompt);

        int maxRetries = 2; // Maximum 2 retries (up to 3 total attempts)
        int attempt = 0;
        Exception lastException = null;

        while (attempt <= maxRetries) {
            long startTime = System.currentTimeMillis();
            try {
                URI uri = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                        .path("/v1beta/models/" + properties.getModel() + ":generateContent")
                        .queryParam("key", properties.getKey())
                        .build()
                        .toUri();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                HttpEntity<GeminiGenerateRequest> entity = new HttpEntity<>(geminiRequest, headers);

                ResponseEntity<GeminiGenerateResponse> response = restTemplate.postForEntity(
                        uri,
                        entity,
                        GeminiGenerateResponse.class
                );

                long latency = System.currentTimeMillis() - startTime;
                log.info("Gemini API attempt {} completed with status {} in {}ms", attempt + 1, response.getStatusCode(), latency);

                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    int statusCode = response.getStatusCode().value();
                    if (isTransientStatusCode(statusCode) && attempt < maxRetries) {
                        long delay = getBackoffDelayWithJitter(attempt, null);
                        log.warn("Gemini API returned transient status {}. Retrying attempt {} in {}ms...", statusCode, attempt + 2, delay);
                        sleepSilently(delay);
                        attempt++;
                        continue;
                    }
                    log.warn("Gemini API non-successful response (status: {}). Providing curated fallback.", response.getStatusCode());
                    return generateCuratedFallback(request, "Gemini API returned status " + response.getStatusCode() + ". Fallback invoked.");
                }

                String jsonText = response.getBody().getFirstText();
                if (jsonText == null || jsonText.trim().isEmpty()) {
                    log.warn("Empty response text from Gemini API. Falling back to curated recommendations.");
                    return generateCuratedFallback(request, "Empty response from AI model. Fallback invoked.");
                }

                String cleanJson = cleanCodeFences(jsonText);
                GeminiRecommendationResult result = objectMapper.readValue(cleanJson, GeminiRecommendationResult.class);

                if (result == null || result.getDestinations() == null || result.getDestinations().isEmpty()) {
                    log.warn("Gemini response parsed with 0 destinations. Falling back to curated recommendations.");
                    return generateCuratedFallback(request, "AI model produced no destination matches. Fallback invoked.");
                }

                // Deduplicate destinations by normalized place name
                List<GeminiDestinationItem> uniqueDestinations = new ArrayList<>();
                Set<String> seenPlaces = new HashSet<>();
                for (GeminiDestinationItem item : result.getDestinations()) {
                    if (item.getPlace() != null && !item.getPlace().trim().isEmpty()) {
                        String norm = item.getPlace().trim().toLowerCase();
                        if (!seenPlaces.contains(norm)) {
                            seenPlaces.add(norm);
                            uniqueDestinations.add(item);
                        }
                    }
                }

                if (uniqueDestinations.isEmpty()) {
                    return generateCuratedFallback(request, "AI destinations had empty names. Fallback invoked.");
                }

                result.setDestinations(uniqueDestinations);
                result.setFallback(false);
                result.setFallbackReason(null);

                log.info("Successfully generated {} unique destination recommendations via Gemini API [{}]",
                        uniqueDestinations.size(), properties.getModel());
                return result;

            } catch (HttpStatusCodeException httpEx) {
                lastException = httpEx;
                int statusCode = httpEx.getStatusCode().value();
                long latency = System.currentTimeMillis() - startTime;
                log.warn("Gemini API HTTP {} error on attempt {} (latency: {}ms): {}", statusCode, attempt + 1, latency, httpEx.getStatusText());

                // Permanent client errors (400, 401, 403, 404): Fail fast immediately, do NOT retry!
                if (statusCode == 400 || statusCode == 401 || statusCode == 403 || statusCode == 404) {
                    log.error("Permanent Gemini API client error ({}). Key or request invalid. Halting retries immediately.", statusCode);
                    return generateCuratedFallback(request, "Gemini API client authentication error (" + statusCode + "). Please verify GEMINI_API_KEY.");
                }

                // Transient errors (429, 500, 502, 503, 504): Retry with backoff & jitter
                if (attempt < maxRetries && isTransientStatusCode(statusCode)) {
                    long delay = getBackoffDelayWithJitter(attempt, httpEx);
                    log.info("Transient Gemini error ({}). Retrying attempt {} in {}ms...", statusCode, attempt + 2, delay);
                    sleepSilently(delay);
                    attempt++;
                } else {
                    break;
                }

            } catch (ResourceAccessException resEx) {
                // Network timeout or connection drop: Retry transiently
                lastException = resEx;
                long latency = System.currentTimeMillis() - startTime;
                log.warn("Gemini API connection/timeout error on attempt {} (latency: {}ms): {}", attempt + 1, latency, resEx.getMessage());

                if (attempt < maxRetries) {
                    long delay = getBackoffDelayWithJitter(attempt, null);
                    log.info("Retrying connection error attempt {} in {}ms...", attempt + 2, delay);
                    sleepSilently(delay);
                    attempt++;
                } else {
                    break;
                }

            } catch (Exception ex) {
                lastException = ex;
                log.error("Unexpected error in Gemini service on attempt {}: {}", attempt + 1, ex.getMessage());
                break;
            }
        }

        String errorMsg = lastException != null ? lastException.getMessage() : "Unknown Gemini failure";
        log.warn("Gemini attempts exhausted or unrecoverable error encountered: {}. Providing robust curated fallback.", errorMsg);
        return generateCuratedFallback(request, "AI model temporarily unavailable (" + errorMsg + "). Showing curated travel intelligence.");
    }

    private boolean isTransientStatusCode(int statusCode) {
        return statusCode == 429 || statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    private long getBackoffDelayWithJitter(int attempt, HttpStatusCodeException ex) {
        if (ex != null && ex.getResponseHeaders() != null) {
            String retryAfter = ex.getResponseHeaders().getFirst("Retry-After");
            if (retryAfter != null) {
                try {
                    long seconds = Long.parseLong(retryAfter.trim());
                    return Math.min(seconds * 1000L, 5000L);
                } catch (NumberFormatException ignored) {}
            }
        }
        long base = 1000L * (1L << attempt); // 1000ms, 2000ms
        long jitter = (long) (Math.random() * 400); // 0-400ms random jitter
        return base + jitter;
    }

    private void sleepSilently(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String buildPrompt(RecommendationRequest req) {
        String budgetType = req.isGroupBudget() ? "Total group budget" : "Budget per person";
        String interestList = String.join(", ", req.getInterests());
        String travelDates = (req.getTravelDates() != null && !req.getTravelDates().trim().isEmpty())
                ? req.getTravelDates().trim()
                : "Flexible upcoming season";

        return """
                You are an expert Indian travel planner and budget analyst for the whereNxt travel platform.
                A traveler has provided the following preferences:
                - Origin / Starting City: %s
                - Stated Budget: ₹%d (%s)
                - Number of Travelers: %d
                - Trip Duration: %d days
                - Travel Dates / Season: %s
                - Travel Passions / Interests: %s
                - Pace of Travel: %s
                - Transport Preference: %s
                - Stay Style: %s
                - Traveling Companion: %s
                - Destination / Region Preference (if any): %s
                - Special Requests / Dietary / Accessibility Notes: %s

                Provide 3 distinct and exciting Indian travel destination recommendations specifically tailored to these inputs.
                For each destination, provide:
                1. "place": Destination city or town name (e.g. "Jaipur", "Udaipur", "Alappuzha", "Varanasi", "Munnar", "Hampi", "Rishikesh", "Gokarna", "Srinagar", "Manali", "Coorg").
                2. "state": The Indian state.
                3. "match": Match percentage as an integer between 82 and 98 based on user vibe and budget.
                4. "recommendedDuration": e.g. "%d days".
                5. "summary": A rich, evocative 1-2 sentence description highlighting why this place fits them.
                6. "tags": 2-3 relevant tags matching their interests (e.g. "Heritage", "Culinary", "Adventure", "Nature", "Wellness", "Spiritual").
                7. "activities": 3-4 specific attractions and activities with genuine local landmark names.
                8. "travelTips": 2-3 practical local travel tips.
                9. "costEstimates": Realistic estimates in Indian Rupees (INR) across low, typical, and high ranges for:
                   - transport (intercity transit from starting city)
                   - accommodation (stay style specified)
                   - food (dining and local specialties)
                   - localTravel (autos, local taxis, transit)
                   - activities (sightseeing, entries, guides)
                   - miscellaneous (shopping, emergency buffer, local tips)
                   - total (sum of above)
                   - perPerson (total divided by number of travelers)
                10. "dayByDayItinerary": Exactly %d days of itinerary. Each item has:
                   - "day": e.g. "Day 1"
                   - "title": Captivating title for that day
                   - "detail": 1-2 sentences of specific experiences and places
                11. "assumptions": 2-3 clear assumptions (e.g. "Off-peak travel dates assumed", "Train travel in 3AC tier").
                12. "uncertaintyMarkers": 1-2 uncertainty markers (e.g. "Flight prices fluctuate closer to departure date").

                Respond strictly with valid JSON conforming to:
                {
                  "destinations": [
                    {
                      "place": "string",
                      "state": "string",
                      "match": 95,
                      "recommendedDuration": "%d days",
                      "summary": "string",
                      "tags": ["string"],
                      "activities": ["string"],
                      "travelTips": ["string"],
                      "costEstimates": {
                        "transport": {"low": 3000, "typical": 5000, "high": 8000, "currency": "INR"},
                        "accommodation": {"low": 6000, "typical": 10000, "high": 16000, "currency": "INR"},
                        "food": {"low": 3000, "typical": 5000, "high": 8000, "currency": "INR"},
                        "localTravel": {"low": 1500, "typical": 2500, "high": 4000, "currency": "INR"},
                        "activities": {"low": 2000, "typical": 3500, "high": 6000, "currency": "INR"},
                        "miscellaneous": {"low": 1000, "typical": 2000, "high": 3500, "currency": "INR"},
                        "total": {"low": 16500, "typical": 28000, "high": 45500, "currency": "INR"},
                        "perPerson": {"low": 8250, "typical": 14000, "high": 22750, "currency": "INR"}
                      },
                      "dayByDayItinerary": [
                        {"day": "Day 1", "title": "string", "detail": "string"}
                      ],
                      "assumptions": ["string"],
                      "uncertaintyMarkers": ["string"]
                    }
                  ]
                }
                """.formatted(
                req.getStartingCity(),
                req.getBudget(),
                budgetType,
                req.getTravelers(),
                req.getTripLengthDays(),
                travelDates,
                interestList.isEmpty() ? "General Exploration" : interestList,
                req.getPace(),
                req.getTransport(),
                req.getStayStyle(),
                req.getCompanion(),
                req.getDestinationPreference() != null && !req.getDestinationPreference().isEmpty() ? req.getDestinationPreference() : "Any destination across India",
                req.getNotes() != null && !req.getNotes().isEmpty() ? req.getNotes() : "None",
                req.getTripLengthDays(),
                Math.min(req.getTripLengthDays(), 5),
                req.getTripLengthDays()
        );
    }

    private String cleanCodeFences(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private static final List<DestinationTemplate> DESTINATION_CATALOG = List.of(
            new DestinationTemplate("Goa", "Goa", List.of("Adventure", "Culinary", "Nature", "Wellness"),
                    "Sun-washed beaches, vibrant coastal shacks, Portuguese heritage and restorative ocean breezes.",
                    List.of("Scuba diving and water sports at Grande Island", "Old Goa heritage churches tour", "Sunset river cruise on the Mandovi", "Spice plantation tour with authentic Goan lunch"),
                    List.of("Rent a two-wheeler for effortless coastal hopping", "Visit South Goa for quiet uncrowded sands"),
                    List.of("Coastal Arrival & Sunsets", "Historic Churches & Spice Trails", "Water Adventures & Beachfront Dining", "South Goa Serenity"),
                    List.of("Arrive, check into beachfront stay, stroll along Calangute or Palolem beach and watch sunset.", "Explore Basilica of Bom Jesus, Se Cathedral and tour a lush organic spice plantation.", "Kayak through backwater mangroves, try surfing or catamaran rides, followed by fresh seafood.", "Explore Cabo de Rama fort and enjoy a quiet evening along butterfly beach."),
                    0.22, 0.35, 0.22, 0.10, 0.11),

            new DestinationTemplate("Manali", "Himachal Pradesh", List.of("Adventure", "Nature"),
                    "Towering snow peaks, cedar forest trails and crisp mountain air tailored for thrilling high-altitude escapes.",
                    List.of("Solang Valley paragliding and zorbing", "Jogini waterfall trek", "Rohtang Pass snow point excursion", "Old Manali bohemian café trail"),
                    List.of("Book Rohtang Pass permit 2-3 days in advance", "Acclimatize on Day 1 before strenuous treks"),
                    List.of("Mountain Arrival & Mall Road", "Solang Valley Adventures", "Alpine Waterfalls & Old Town", "Atal Tunnel & Sissu Valley"),
                    List.of("Arrive in Manali, settle in, visit Hadimba Temple and enjoy hot trout along Mall Road.", "High-adrenaline day in Solang with paragliding, ATV rides and snow adventures.", "Hike through apple orchards to Jogini Falls and explore Old Manali's cozy music cafés.", "Cross Atal Tunnel into picturesque Lahaul Valley and marvel at Sissu waterfall."),
                    0.24, 0.32, 0.22, 0.10, 0.12),

            new DestinationTemplate("Rishikesh", "Uttarakhand", List.of("Adventure", "Spiritual", "Wellness"),
                    "Sacred river ghats, exhilarating Himalayan white-water rapids and restorative sunrise yoga sanctuaries.",
                    List.of("White-water rafting from Marine Drive to NIM Beach", "Triveni Ghat evening Maha Aarti", "Neer Garh waterfall jungle trek", "Beatles Ashram graffiti walk"),
                    List.of("Best rafting season is October through May", "Carry river shoes and quick-dry apparel"),
                    List.of("Sacred River Sanctuary", "White Water & Canyon Thrills", "Ashram Trails & Sunset Aarti", "Mountain Heights & Stays"),
                    List.of("Arrive by the Ganges, walk across Ram Jhula, attend evening riverside meditation.", "Tackle Grade III rapids on a 16km rafting expedition, followed by cliff jumping.", "Explore historic Beatles Ashram and witness the ceremonial Ganga Aarti at Triveni Ghat.", "Hike up to Kunjapuri Temple for sunrise over Himalayan peaks."),
                    0.20, 0.30, 0.22, 0.12, 0.16),

            new DestinationTemplate("Jaipur", "Rajasthan", List.of("Heritage", "Culinary"),
                    "Rose-hued palaces, artisan bazaars and slow haveli dinners—balanced into one vibrant royal weekend.",
                    List.of("Sunrise at Hawa Mahal", "Amber Fort royal history walk", "Block printing workshop in Sanganer", "Chokhi Dhani cultural dining"),
                    List.of("Purchase the composite ticket for major monuments to save time", "Bargain respectfully in Johari and Bapu Bazaars"),
                    List.of("Old City & Royal Stories", "Forts Above the City", "Craft, Colour & Cuisine", "Quiet Courtyards & Stepwells"),
                    List.of("Hawa Mahal at first light, City Palace courtyards and a guided spice walk through Johari Bazaar.", "Amber Fort, stepwells of Panna Meena Kund and sunset from Nahargarh with a traditional dinner.", "Block-print workshop, textile shopping and an intimate Rajasthani tasting menu at a restored haveli.", "Morning at Jal Mahal promenade, Albert Hall Museum and evening tea overlooking Aravalli hills."),
                    0.20, 0.32, 0.24, 0.10, 0.14),

            new DestinationTemplate("Alappuzha", "Kerala", List.of("Wellness", "Nature"),
                    "Quiet backwaters, coconut-lined canals and restorative coastal flavours at a beautifully unhurried pace.",
                    List.of("Private canoe ride through Kuttanad village canals", "Ayurvedic oil massage and rejuvenation", "Marari beach sunset walk", "Kerala sadhya lunch on banana leaf"),
                    List.of("Opt for a day canoe over an overnight houseboat to explore narrower canals responsibly", "Carry mosquito repellent and linen clothing"),
                    List.of("Arrive by the Water", "Backwater Immersion", "Coast & Calm", "Spice Groves & Village Craft"),
                    List.of("Check into a canal-side stay, cycle the village lanes and watch sunset by canoe.", "A responsible houseboat journey with a home-cooked Kerala fish curry lunch.", "Marari beach walk, authentic Ayurvedic massage and a seafood dinner by the shore.", "Coir village weaving demonstration, kayaking through tranquil backwaters and fresh toddy tasting."),
                    0.22, 0.35, 0.20, 0.09, 0.14),

            new DestinationTemplate("Udaipur", "Rajasthan", List.of("Heritage", "Wellness", "Culinary"),
                    "Shimmering lakes, marble courtyards, romantic rooftop views and peaceful sunset boat journeys.",
                    List.of("Lake Pichola sunset boat cruise", "City Palace museum exploration", "Saheliyon-ki-Bari garden walk", "Dharohar folk dance at Bagore Ki Haveli"),
                    List.of("Reserve rooftop dining facing the illuminated palace in advance", "Walk the old pedestrian footbridge near Chandpole"),
                    List.of("Lakeside Arrival & Palace Views", "Royal Heritage & Courtyards", "Artisan Lanes & Folk Culture", "Monsoon Palace Sunset"),
                    List.of("Arrive in the city of lakes, check in near Lake Pichola and watch evening reflections on the water.", "Explore City Palace complex, Jagdish Temple and take a private wooden boat to Jagmandir.", "Visit Bagore Ki Haveli, enjoy a miniature painting workshop and taste Mewari delicacies.", "Drive up to Sajjangarh Monsoon Palace for panoramic Aravalli mountain sunsets."),
                    0.22, 0.34, 0.22, 0.10, 0.12),

            new DestinationTemplate("Munnar", "Kerala", List.of("Nature", "Wellness"),
                    "Emerald tea slopes, misty mountain roads, cascading waterfalls and refreshing cool hill station retreats.",
                    List.of("Tea museum and plantation walking trail", "Eravikulam National Park Nilgiri Tahr safari", "Mattupetty Dam boat ride", "Spice plantation and herbal garden tour"),
                    List.of("Mornings can be chilly; carry a warm fleece", "Book Eravikulam National Park entry online"),
                    List.of("Mist & Mountains", "Tea Trails & Wildlife", "Lakes & Waterfalls", "Spice Gardens & Quiet Slopes"),
                    List.of("Drive through winding tea hills, check into a plantation estate and walk among tea bushes.", "Morning safari in Eravikulam National Park and visit the historic Tea Processing Museum.", "Visit Mattupetty Dam, Echo Point, and enjoy panoramic views from Kundala Lake.", "Tour organic cardamon and pepper groves followed by traditional Ayurvedic rejuvenation."),
                    0.24, 0.34, 0.20, 0.10, 0.12),

            new DestinationTemplate("Hampi", "Karnataka", List.of("Heritage", "Adventure"),
                    "Monumental boulder landscapes, monolithic Vijayanagara temples and unforgettable sunsets across ancient ruins.",
                    List.of("Coracle ride across Tungabhadra River", "Virupaksha Temple and Hampi Bazaar tour", "Bouldering and Matanga Hill sunrise trek", "Vittala Temple Stone Chariot exploration"),
                    List.of("Rent a bicycle or moped to navigate between monuments", "Carry a wide-brim hat and ample water for afternoon walks"),
                    List.of("Empire of Stone & River", "Royal Ruins & Stone Chariot", "Sunrise Peaks & Boulders", "Hippy Island & Paddy Fields"),
                    List.of("Arrive in Hampi, cross Tungabhadra on a traditional coracle and climb Hemakuta Hill for sunset.", "Explore the majestic Vittala Temple complex, King's Balance and Lotus Mahal.", "Dawn trek up Matanga Hill for 360-degree vistas across boulders, followed by an archaeological museum visit.", "Cycle across the river through lush paddy fields and boulder-strewn villages to Sanapur Lake."),
                    0.20, 0.30, 0.22, 0.12, 0.16),

            new DestinationTemplate("Varanasi", "Uttar Pradesh", List.of("Spiritual", "Culture"),
                    "Ancient river ghats, dawn on the sacred Ganges and timeless spiritual traditions guided thoughtfully.",
                    List.of("Dawn rowing boat ride along Dashashwamedh to Assi Ghat", "Evening Ganga Aarti ceremony from boat", "Weaver workshop visit for Banarasi silk", "Sarnath Buddhist peace complex"),
                    List.of("Wake up before 5:30 AM for the most serene river atmosphere", "Try Blue Lassi and kachori jalebi in Vishwanath Gali"),
                    List.of("The River Awakens", "Stories & Ceremonies", "Local Rhythms & Music", "Sarnath & Hidden Shrines"),
                    List.of("Sunrise wooden boat ride, breakfast in the old city and a walk through hidden riverside shrines.", "Silk weaving studio in Sarai Mohana, excursion to Sarnath and evening Ganga Aarti from a boat.", "Morning classical music gathering, street culinary exploration and final walk along southern ghats.", "Explore Ramnagar Fort across the river and discover historic havelis in Bengali Tola."),
                    0.20, 0.28, 0.22, 0.12, 0.18),

            new DestinationTemplate("Gokarna", "Karnataka", List.of("Wellness", "Nature", "Adventure"),
                    "Secluded crescent beaches, rugged cliffside trails, bohemian shacks and peaceful seaside sunsets.",
                    List.of("Five-beach cliff trek (Kudle to Paradise)", "Sunset beach yoga and wellness session", "Mahabaleshwar Temple cultural visit", "Dolphin spotting boat ride"),
                    List.of("Carry hiking sandals for the cliff trail between beaches", "Paradise beach is best visited by ferry or morning hike"),
                    List.of("Cliff Paths & Ocean Breezes", "Beach Trekking Adventure", "Coastal Calm & Yoga", "Secret Coves & Seafood"),
                    List.of("Arrive in Gokarna, walk along Kudle Beach, watch sunset from a cliff café.", "Full-day beach trek crossing Om Beach, Half Moon Beach and secluded Paradise Beach.", "Sunrise yoga overlooking the Arabian Sea, visit town temple and enjoy fresh coastal cuisine.", "Ferry ride along the coastline for dolphin spotting and relaxing swim at Nirvana Beach."),
                    0.20, 0.32, 0.22, 0.10, 0.16),

            new DestinationTemplate("Coorg", "Karnataka", List.of("Nature", "Culinary", "Wellness"),
                    "Fragrant coffee plantations, cascading waterfalls, Kodava culinary traditions and lush Western Ghats trails.",
                    List.of("Coffee and spice estate plantation walk", "Abbey Falls and Raja's Seat sunset", "Dubare Elephant Camp interaction", "Authentic Kodava pandi curry dining"),
                    List.of("Opt for an estate homestay for the most authentic local experience", "November to March offers ideal weather"),
                    List.of("Coffee Aromas & Hills", "Waterfalls & Viewpoints", "Elephant Encounters", "Sacred Kaveri & Trails"),
                    List.of("Arrive in Madikeri, check into a working coffee estate and take a guided coffee bean tour.", "Visit roaring Abbey Falls, Madikeri Fort and watch sunset over rolling hills at Raja's Seat.", "Morning visit to Dubare Elephant Camp and river rafting on the Cauvery.", "Trek through Talakaveri sanctuary, the birthplace of River Kaveri, and enjoy a traditional Kodava feast."),
                    0.20, 0.34, 0.22, 0.10, 0.14),

            new DestinationTemplate("Amritsar", "Punjab", List.of("Spiritual", "Culinary", "Heritage"),
                    "Golden serenity at the Harmandir Sahib, legendary culinary trails and patriotic ceremony at Wagah.",
                    List.of("Golden Temple meditation and community langar", "Wagah Border flag lowering ceremony", "Jallianwala Bagh memorial visit", "Legendary Amritsari kulcha and lassi food crawl"),
                    List.of("Cover your head and remove footwear inside the Golden Temple complex", "Arrive at Wagah Border by 3:30 PM for good seating"),
                    List.of("Golden Light & Community", "Patriotism & Historic Remembrances", "Food Trails & Bazaars", "Village Culture"),
                    List.of("Visit the Golden Temple at twilight, listen to Gurbani hymns and volunteer at the langar hall.", "Pay respects at Jallianwala Bagh, visit Partition Museum and witness electric Wagah Border ceremony.", "Walk through Hall Bazaar tasting crisp kulchas, makki di roti and rich sweet lassi.", "Experience rural Punjabi heritage at Sadda Pind with folk dance and handicraft workshops."),
                    0.20, 0.28, 0.26, 0.10, 0.16),

            new DestinationTemplate("Darjeeling", "West Bengal", List.of("Nature", "Culture", "Heritage"),
                    "Panoramic Kanchenjunga sunrises, fragrant Himalayan tea gardens and historic toy train steam journeys.",
                    List.of("Tiger Hill sunrise over Mt. Kanchenjunga", "Heritage Darjeeling Himalayan Railway joyride", "Happy Valley Tea Estate factory tour", "Padmaja Naidu Himalayan Zoo (Red Pandas)"),
                    List.of("Wake up at 3:30 AM for the clearest Tiger Hill sunrise view", "Try piping hot momos and thukpa along Mall Road"),
                    List.of("Himalayan Dawn & Peaks", "Steam Trains & Tea Estates", "Monasteries & Tibetan Craft", "Alpine Peace"),
                    List.of("Early sunrise drive to Tiger Hill to watch the first light illuminate Kanchenjunga, followed by Batasia Loop.", "Ride the world-famous steam toy train and tour century-old Happy Valley Tea Estate.", "Visit Himalayan Mountaineering Institute, the snow leopard conservation center and Ghoom Monastery.", "Relax at Chowrasta square, shop for authentic first-flush tea and enjoy sunset views."),
                    0.24, 0.32, 0.22, 0.10, 0.12),

            new DestinationTemplate("Meghalaya", "Meghalaya", List.of("Nature", "Adventure"),
                    "Ancient living root bridges, crystal-clear rivers, dramatic canyon waterfalls and cloud-kissed peaks.",
                    List.of("Double Decker Living Root Bridge trek in Nongriat", "Boating on glassy Dawki Umngot River", "Nohkalikai Falls and Mawsmai Cave exploration", "Cleanest village tour of Mawlynnong"),
                    List.of("Pack sturdy trekking shoes with excellent grip for the 3,000 root bridge steps", "Carry lightweight waterproof jacket"),
                    List.of("Abode of Clouds", "Living Root Bridges Trek", "Crystal Waters & Border Villages", "Caves & Waterfalls"),
                    List.of("Arrive in Shillong, drive to Cherrapunji past mist-covered gorges and dramatic viewpoints.", "Full-day trek down the valley to the world-famous Double Decker Living Root Bridge and rainbow falls.", "Drive to Dawki for boating on the transparent Umngot River and visit border village Mawlynnong.", "Explore the limestone formations of Mawsmai Cave and admire cascading Seven Sisters Falls."),
                    0.26, 0.30, 0.20, 0.10, 0.14),

            new DestinationTemplate("Agra", "Uttar Pradesh", List.of("Heritage", "Culture"),
                    "Timeless Mughal architectural majesty, the eternal ivory splendor of the Taj Mahal and vibrant local petha bazaars.",
                    List.of("Sunrise at the Taj Mahal", "Agra Fort royal red sandstone palaces walk", "Sunset views from Mehtab Bagh across the Yamuna", "Kinari Bazaar traditional craft and culinary walk"),
                    List.of("Taj Mahal is closed on Fridays; plan your visit accordingly", "Early sunrise entry offers the calmest atmosphere and best light"),
                    List.of("Monument of Love", "Mughal Dynasties & Forts", "Imperial Gardens & Crafts", "Fatehpur Sikri Excursion"),
                    List.of("Enter the Taj Mahal at dawn as the morning light illuminates the marble dome, followed by a local breakfast.", "Explore the immense halls and courtyards of Agra Fort and visit historic Itmad-ud-Daulah.", "Sunset walk in Mehtab Bagh gardens and sampling famous Agra petha along Sadar Bazaar.", "Day trip to the abandoned red sandstone Mughal city of Fatehpur Sikri and Buland Darwaza."),
                    0.22, 0.30, 0.24, 0.10, 0.14),

            new DestinationTemplate("Srinagar", "Jammu and Kashmir", List.of("Nature", "Wellness", "Culture"),
                    "Serene wooden shikaras on Dal Lake, Mughal terraced gardens and snow-crowned Himalayan alpine vistas.",
                    List.of("Shikara boat ride and floating market at dawn", "Stay in a traditional carved cedarwood houseboat", "Shalimar and Nishat Mughal Garden stroll", "Gulmarg Gondola snow-point excursion"),
                    List.of("Pre-book Gulmarg gondola tickets well ahead online", "Savor authentic Kashmiri Wazwan and Kahwa saffron tea"),
                    List.of("Valley of Serenity & Dal Lake", "Houseboat Living & Floating Bazaars", "Mughal Terraces & Heritage", "Alpine Meadows of Gulmarg"),
                    List.of("Arrive in Srinagar, check into a traditional cedar houseboat on Dal Lake and watch sunset from a shikara.", "Morning shikara ride through floating vegetable markets and explore Old Srinagar's wooden mosques.", "Walk through terraced Mughal Gardens Shalimar Bagh and Nishat Bagh with Zabarwan mountain backdrops.", "Scenic day trip to Gulmarg, ride the highest cable car in Asia to Apharwat Peak."),
                    0.26, 0.32, 0.20, 0.10, 0.12),

            new DestinationTemplate("Ladakh", "Ladakh", List.of("Adventure", "Nature", "Spiritual"),
                    "Dramatic high-altitude moonscapes, sapphire-blue Pangong Lake, high mountain passes and cliffside Buddhist monasteries.",
                    List.of("Pangong Tso alpine lake expedition", "Khardung La high-altitude pass crossing", "Thiksey and Hemis monastery morning prayers", "Nubra Valley sand dunes and double-humped camel safari"),
                    List.of("Dedicate your first 24-48 hours strictly to acclimatizing in Leh (3,500m)", "Carry warm layers even in summer months"),
                    List.of("Leh Acclimatization & Monasteries", "World's Highest Passes to Nubra", "Pangong Tso Blue Waters", "Cliffside Gompas & Return"),
                    List.of("Arrive at Leh Kushok Bakula airport, rest and acclimatize, evening walk to Shanti Stupa for sunset.", "Cross Khardung La pass into Nubra Valley, explore Diskit Monastery and ride Bactrian camels at Hunder.", "Drive along the Shyok River to surreal Pangong Tso lake; stay in lakeside eco-camps under starlit skies.", "Attend dawn chanting at Thiksey Monastery and explore Leh Palace and central market."),
                    0.28, 0.30, 0.20, 0.10, 0.12),

            new DestinationTemplate("Ooty", "Tamil Nadu", List.of("Nature", "Wellness"),
                    "Rolling Nilgiri tea hills, eucalyptus-scented breezes, heritage mountain toy trains and cool hill station tranquility.",
                    List.of("Nilgiri Mountain Railway toy train joyride", "Botanical Gardens and Doddabetta Peak", "Pykara Lake speedboating and waterfalls", "Tea plantation walking tour and factory tasting"),
                    List.of("Toy train tickets from Mettupalayam/Coonoor sell out fast; reserve in advance", "Evenings can be brisk; carry a sweater"),
                    List.of("Nilgiri Mountain Rail & Arrival", "Botanical Splendors & Peaks", "Lakes, Waterfalls & Pines", "Tea Estate Aromas"),
                    List.of("Ride the UNESCO heritage steam train into Ooty, check into a colonial hill cottage, and walk around Ooty Lake.", "Visit the historic Government Botanical Garden and take in panoramic views from Doddabetta Peak.", "Excursion to pristine Pykara Lake and waterfalls surrounded by quiet pine forests.", "Tour a working tea factory, learn orthodox tea manufacturing, and visit nearby Coonoor viewpoints."),
                    0.22, 0.34, 0.22, 0.10, 0.12),

            new DestinationTemplate("Pondicherry", "Puducherry", List.of("Wellness", "Heritage", "Culinary"),
                    "Sun-kissed French colonial quarters, vibrant bougainvillea streets, tranquil ashrams and peaceful coastal promenades.",
                    List.of("Bicycle tour through French White Town", "Auroville Matrimandir meditation and visit", "Sunrise promenade walk along the Bay of Bengal", "French-Creole bakery and bistro crawl"),
                    List.of("Rent a bicycle or pastel scooter to effortlessly explore White Town", "Pre-book Matrimandir inner chamber entry if planning meditation"),
                    List.of("White Town & French Heritage", "Auroville & Peaceful Living", "Coastal Beaches & Surfing", "Creole Flavors & Cafes"),
                    List.of("Check into a restored French heritage villa, cycle along cobbled streets and watch twilight on the Promenade.", "Visit Auroville international township, explore sustainable architecture and meditate at Matrimandir.", "Morning walk at Paradise Beach, try surfing at Serenity Beach and explore Tamil Quarter markets.", "Enjoy café culture tasting fresh croissants, Creole dishes and artisanal gelato."),
                    0.22, 0.32, 0.24, 0.10, 0.12),

            new DestinationTemplate("Shimla", "Himachal Pradesh", List.of("Nature", "Heritage", "Adventure"),
                    "Colonial hill architecture, pine-forested ridges, panoramic Himalayan snow views and refreshing mountain charm.",
                    List.of("Stroll along the pedestrian Ridge and historic Mall Road", "Jakhoo Hill Hanuman Temple hike", "Kalka-Shimla UNESCO toy train ride", "Kufri alpine nature park and viewpoints"),
                    List.of("Mall Road is completely pedestrianized; wear comfortable walking shoes", "Winter (Dec-Feb) offers snowfall opportunities"),
                    List.of("Queen of the Hills & Mall Road", "Jakhoo Temple & Forest Trails", "Colonial Heritage & Viceregal Lodge", "Kufri Heights & Pines"),
                    List.of("Arrive via toy train, check into a hillside lodge, stroll past the neo-Gothic Christ Church along the Ridge.", "Morning forest trail walk up Jakhoo Hill for sweeping valley views and playful monkeys.", "Tour the magnificent Scottish-baronial Viceregal Lodge and Indian Institute of Advanced Study.", "Drive to Kufri for horseback riding through cedar forests and panoramic Himalayan views."),
                    0.24, 0.32, 0.22, 0.10, 0.12),

            new DestinationTemplate("Jaisalmer", "Rajasthan", List.of("Adventure", "Heritage", "Culture"),
                    "The shimmering Golden Fort, undulating Thar desert sand dunes, starlit camping and rich Rajasthani folk music.",
                    List.of("Sam Sand Dunes sunset camel and jeep safari", "Overnight glamping under desert stars with folk music", "Living Golden Fort and Jain Temples walk", "Patwon Ki Haveli intricate sandstone carving tour"),
                    List.of("October to March offers pleasant desert temperatures", "Choose a non-motorized sunset dune spot for tranquility"),
                    List.of("Golden Fort & Sandstone Lanes", "Thar Desert Dunes & Starlit Camp", "Ornate Havelis & Desert Culture", "Sacred Lakes & Ghost Villages"),
                    List.of("Arrive in the golden city, explore the living Jaisalmer Fort with its bustling shops and royal palace.", "Drive to Sam Sand Dunes, ride a camel into the sunset, enjoy Kalbelia dance around a desert campfire.", "Explore exquisite sandstone lacework at Patwon Ki Haveli and Salim Singh Ki Haveli.", "Visit tranquil Gadisar Lake at sunrise and discover the abandoned heritage village of Kuldhara."),
                    0.22, 0.32, 0.24, 0.10, 0.12),

            new DestinationTemplate("Jabalpur", "Madhya Pradesh", List.of("Nature", "Adventure", "Heritage"),
                    "Dramatic white marble river gorges, cascading Dhuandhar waterfalls, prehistoric rock formations and peaceful holy Narmada ghats.",
                    List.of("Cable car ride and boat cruise amidst Bhedaghat Marble Rocks", "Witness roaring Dhuandhar Falls on the holy Narmada River", "Explore 11th-century Madan Mahal Fort and balancing rock", "Evening Maha Aarti at Gwarighat and local street food trail"),
                    List.of("Boating through marble rocks under a full moon is an unforgettable experience", "Try hot khowa jalebi and samosas in the old town"),
                    List.of("Holy Narmada & Marble Gorges", "Dhuandhar Roar & Cable Car", "Ancient Forts & Balancing Rocks", "Sacred Ghats & Evening Aarti"),
                    List.of("Arrive in Jabalpur, take a scenic wooden rowboat through the towering marble gorge at Bhedaghat.", "Experience the misty spray of Dhuandhar Falls and take the aerial ropeway across the gorge.", "Visit historic Madan Mahal Fort, Chausath Yogini Temple and the geological wonder Balancing Rock.", "Attend spiritual evening Aarti along Gwarighat and savor street food along civic center."),
                    0.20, 0.30, 0.24, 0.12, 0.14),

            new DestinationTemplate("Pachmarhi", "Madhya Pradesh", List.of("Nature", "Adventure", "Wellness"),
                    "The verdant Queen of the Satpuras, featuring mist-shrouded waterfalls, ancient Pandava caves, sal forest viewpoints and tranquil pine hills.",
                    List.of("Swim and relax at Bee Falls and Silver Falls (Rajat Prapat)", "Spectacular sunset from Dhoopgarh (highest peak in MP)", "Explore ancient Buddhist and Pandava rock-cut caves", "Jatashankar cave shrine and Mahadeo temple trekking"),
                    List.of("Hire an open 4x4 Gypsy with a registered forest guide for sightseeing", "Dhoopgarh sunset requires an early arrival to secure a good spot"),
                    List.of("Queen of Satpura Arrival", "Cascading Waterfalls & Natural Pools", "Dhoopgarh Sunset & Forest Vistas", "Ancient Caves & Sacred Shrines"),
                    List.of("Drive through Satpura pine forests to Pachmarhi, check into a colonial bungalow and stroll through the market.", "Trek down to refreshing Bee Falls for a swim, followed by Duchess Falls and Apsara Vihar pool.", "Hike up to Dhoopgarh peak for panoramic Satpura mountain sunsets and starry skies.", "Explore the historic Pandava Caves, Jatashankar ravine and Reechgarh natural amphitheatre."),
                    0.20, 0.32, 0.22, 0.12, 0.14)
    );

    /**
     * Dynamically matches recommendations from the comprehensive Indian destinations catalog
     * based on user keywords, starting city, interests, pace, and budget.
     */
    public GeminiRecommendationResult generateCuratedFallback(RecommendationRequest request, String reason) {
        GeminiRecommendationResult result = new GeminiRecommendationResult();
        result.setFallback(true);
        result.setFallbackReason(reason);

        List<GeminiDestinationItem> list = new ArrayList<>();

        String destinationPref = request.getDestinationPreference() != null ? request.getDestinationPreference().trim().toLowerCase() : "";
        String notes = request.getNotes() != null ? request.getNotes().toLowerCase() : "";
        String userQuery = (notes + " " + destinationPref).trim();

        List<String> userInterests = request.getInterests() != null ? request.getInterests() : Collections.emptyList();
        String startingCity = request.getStartingCity() != null ? request.getStartingCity().trim() : "Bengaluru";

        // Score all templates
        List<ScoredDestination> scoredList = new ArrayList<>();
        for (DestinationTemplate t : DESTINATION_CATALOG) {
            int score = 50;

            // Direct match with explicit destination search/preference
            if (!destinationPref.isEmpty()) {
                if (t.place.toLowerCase().contains(destinationPref) || destinationPref.contains(t.place.toLowerCase()) ||
                        t.state.toLowerCase().contains(destinationPref)) {
                    score += 1000;
                }
            }

            // Direct mention in notes or query
            if (userQuery.contains(t.place.toLowerCase()) || userQuery.contains(t.state.toLowerCase())) {
                score += 300;
            }

            // Keyword triggers
            if (userQuery.contains("beach") || userQuery.contains("sea") || userQuery.contains("ocean") || userQuery.contains("coastal")) {
                if (t.place.equals("Goa") || t.place.equals("Gokarna") || t.place.equals("Alappuzha") || t.place.equals("Pondicherry")) score += 120;
            }
            if (userQuery.contains("mountain") || userQuery.contains("hill") || userQuery.contains("snow") || userQuery.contains("trek") || userQuery.contains("hiking")) {
                if (t.place.equals("Manali") || t.place.equals("Rishikesh") || t.place.equals("Munnar") || t.place.equals("Darjeeling") || t.place.equals("Meghalaya") || t.place.equals("Ladakh") || t.place.equals("Srinagar") || t.place.equals("Shimla") || t.place.equals("Ooty")) score += 120;
            }
            if (userQuery.contains("temple") || userQuery.contains("spiritual") || userQuery.contains("sacred") || userQuery.contains("peace")) {
                if (t.place.equals("Varanasi") || t.place.equals("Rishikesh") || t.place.equals("Amritsar") || t.place.equals("Hampi")) score += 100;
            }
            if (userQuery.contains("palace") || userQuery.contains("royal") || userQuery.contains("fort") || userQuery.contains("heritage")) {
                if (t.place.equals("Jaipur") || t.place.equals("Udaipur") || t.place.equals("Hampi") || t.place.equals("Jaisalmer") || t.place.equals("Agra")) score += 100;
            }
            if (userQuery.contains("desert") || userQuery.contains("sand") || userQuery.contains("dunes") || userQuery.contains("camel")) {
                if (t.place.equals("Jaisalmer") || t.place.equals("Jaipur") || t.place.equals("Ladakh")) score += 150;
            }
            if (userQuery.contains("lake") || userQuery.contains("shikara") || userQuery.contains("boat") || userQuery.contains("backwater")) {
                if (t.place.equals("Alappuzha") || t.place.equals("Udaipur") || t.place.equals("Srinagar") || t.place.equals("Ooty")) score += 120;
            }
            if (userQuery.contains("food") || userQuery.contains("culinary") || userQuery.contains("taste") || userQuery.contains("tea") || userQuery.contains("coffee")) {
                if (t.place.equals("Amritsar") || t.place.equals("Jaipur") || t.place.equals("Coorg") || t.place.equals("Munnar") || t.place.equals("Goa")) score += 80;
            }

            // Interest tag overlap
            for (String interest : userInterests) {
                if (t.tags.stream().anyMatch(tag -> tag.equalsIgnoreCase(interest))) {
                    score += 40;
                }
            }

            // Origin city penalty (don't recommend the city the traveler is leaving from, UNLESS explicitly requested)
            if (destinationPref.isEmpty() && t.place.equalsIgnoreCase(startingCity)) {
                score -= 500;
            }

            scoredList.add(new ScoredDestination(t, score));
        }

        // Sort by score descending
        scoredList.sort((a, b) -> Integer.compare(b.score, a.score));

        // Pick top 3-4 destinations
        int count = Math.min(3, scoredList.size());
        int[] matchPcts = {96, 93, 89, 86};

        int baseBudget = request.getBudget();
        int travelers = Math.max(1, request.getTravelers());
        int days = request.getTripLengthDays();

        for (int i = 0; i < count; i++) {
            DestinationTemplate t = scoredList.get(i).template;
            GeminiDestinationItem item = new GeminiDestinationItem();
            item.setPlace(t.place);
            item.setState(t.state);
            item.setMatch(matchPcts[i]);
            item.setRecommendedDuration(days + " days");
            item.setSummary(t.summary);
            item.setTags(t.tags);
            item.setActivities(t.activities);
            item.setTravelTips(t.travelTips);

            // Calculate costs in INR
            int tTransport = Math.max(2500, (int) (baseBudget * t.defaultTransportWeight));
            int tStay = Math.max(4500, (int) (baseBudget * t.defaultStayWeight));
            int tFood = Math.max(3000, (int) (baseBudget * t.defaultFoodWeight));
            int tLocal = Math.max(1200, (int) (baseBudget * t.defaultLocalWeight));
            int tAct = Math.max(1800, (int) (baseBudget * t.defaultActWeight));
            int tMisc = Math.max(1000, (int) (baseBudget * 0.05));
            int tTotal = tTransport + tStay + tFood + tLocal + tAct + tMisc;

            item.setCostEstimates(createEstimates(tTransport, tStay, tFood, tLocal, tAct, tMisc, tTotal, travelers));

            // Itinerary days
            List<GeminiDayPlan> itinerary = new ArrayList<>();
            for (int d = 0; d < Math.min(days, t.dayThemes.size()); d++) {
                itinerary.add(new GeminiDayPlan("Day " + (d + 1), t.dayThemes.get(d), t.dayDetails.get(d)));
            }
            for (int d = t.dayThemes.size(); d < days; d++) {
                itinerary.add(new GeminiDayPlan("Day " + (d + 1), "Local Exploration & Hidden Gems",
                        "Discover neighborhood cafés, local handicraft artisans, and scenic viewpoints around " + t.place + "."));
            }
            item.setDayByDayItinerary(itinerary);

            item.setAssumptions(List.of(
                    "Calculated for " + travelers + " traveler(s) departing from " + startingCity,
                    "Assumes " + request.getStayStyle() + " and " + request.getTransport() + " transit",
                    reason
            ));
            item.setUncertaintyMarkers(List.of(
                    "Tariffs may fluctuate during weekend or festive holiday periods",
                    "Local travel fares depend on meter vs private taxi hiring"
            ));

            list.add(item);
        }

        result.setDestinations(list);
        return result;
    }

    private CostEstimatesDto createEstimates(int transport, int stay, int food, int local, int act, int misc, int total, int travelers) {
        int safeTravelers = Math.max(1, travelers);
        int pLow = (int) (total * 0.8) / safeTravelers;
        int pTyp = total / safeTravelers;
        int pHigh = (int) (total * 1.3) / safeTravelers;

        return new CostEstimatesDto(
                new CostRangeDto((int) (transport * 0.8), transport, (int) (transport * 1.3)),
                new CostRangeDto((int) (stay * 0.75), stay, (int) (stay * 1.35)),
                new CostRangeDto((int) (food * 0.8), food, (int) (food * 1.25)),
                new CostRangeDto((int) (local * 0.8), local, (int) (local * 1.3)),
                new CostRangeDto((int) (act * 0.75), act, (int) (act * 1.3)),
                new CostRangeDto((int) (misc * 0.7), misc, (int) (misc * 1.4)),
                new CostRangeDto((int) (total * 0.8), total, (int) (total * 1.3)),
                new CostRangeDto(pLow, pTyp, pHigh)
        );
    }

    private static class DestinationTemplate {
        final String place;
        final String state;
        final List<String> tags;
        final String summary;
        final List<String> activities;
        final List<String> travelTips;
        final List<String> dayThemes;
        final List<String> dayDetails;
        final double defaultTransportWeight;
        final double defaultStayWeight;
        final double defaultFoodWeight;
        final double defaultLocalWeight;
        final double defaultActWeight;

        DestinationTemplate(String place, String state, List<String> tags, String summary,
                            List<String> activities, List<String> travelTips,
                            List<String> dayThemes, List<String> dayDetails,
                            double defaultTransportWeight, double defaultStayWeight,
                            double defaultFoodWeight, double defaultLocalWeight,
                            double defaultActWeight) {
            this.place = place;
            this.state = state;
            this.tags = tags;
            this.summary = summary;
            this.activities = activities;
            this.travelTips = travelTips;
            this.dayThemes = dayThemes;
            this.dayDetails = dayDetails;
            this.defaultTransportWeight = defaultTransportWeight;
            this.defaultStayWeight = defaultStayWeight;
            this.defaultFoodWeight = defaultFoodWeight;
            this.defaultLocalWeight = defaultLocalWeight;
            this.defaultActWeight = defaultActWeight;
        }
    }

    private static class ScoredDestination {
        final DestinationTemplate template;
        final int score;

        ScoredDestination(DestinationTemplate template, int score) {
            this.template = template;
            this.score = score;
        }
    }
}
