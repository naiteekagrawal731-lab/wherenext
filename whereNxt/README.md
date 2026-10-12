# YātrāAI - Simplified Travel Recommendation & Cost Estimation Workflow

An intelligent Indian travel planning application that pairs **Google Gemini AI** for personalized destination recommendations and itemized Indian Rupee (INR ₹) cost estimations with the **Google Places API** for real-world coordinates, ratings, user reviews, and verified destination photography.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                   Frontend (HTML/CSS/Vanilla JS)            │
│  - User travel preferences form & client-side validation   │
│  - Destination cards with Google Maps links & photos        │
│  - User budget comparison beside AI estimates               │
│  - Itemized cost breakdowns & day-by-day itineraries       │
└──────────────────────────────▲──────────────────────────────┘
                               │ HTTP / JSON (No DB)
┌──────────────────────────────▼──────────────────────────────┐
│             Spring Boot Backend Intermediary (Java 17)      │
│  TravelRecommendationController                             │
│  ├─ POST /api/recommendations                               │
│  ├─ GET  /api/places/photo (Secure Photo Proxy)             │
│  └─ GET  /api/status                                        │
│                                                             │
│  TravelRecommendationService                                │
│  ├─ GeminiService (Prompts Gemini, validates structured JSON)│
│  └─ GooglePlacesService (Queries Places, fetches photos)   │
└──────────────┬──────────────────────────────┬───────────────┘
               │ Server-side API Key          │ Server-side API Key
               ▼                              ▼
      Google Gemini API              Google Places API
```

---

## Features

1. **Gemini Recommendations & Cost Estimation**:
   - Considers Starting City, Group Size, Trip Duration, Travel Dates, Persona, Transit Style, Accommodation Type, Companions, and Travel Pace.
   - Returns structured JSON with:
     - Name and State
     - Description and Recommendation Reasons
     - Matching Interests
     - Daily Suggested Itinerary & Activities
     - Itemized Cost Breakdown in INR (₹): Round-trip Transit, Accommodation, Food, Local Commute, Entry Fees, Miscellaneous Buffer.
     - Low, Typical, and High Cost Ranges.
     - Per-Person Cost Estimate.
     - Key Cost Assumptions & Uncertainties.
   - Transparently marks prices as estimates rather than booking quotes.

2. **Backend Intermediary (Spring Boot 3.3.4)**:
   - Validates user input preferences with JSR-303 annotations.
   - Protects API keys strictly on the server side (never leaked to browser JavaScript).
   - Validates and parses Gemini's response without inventing artificial scoring or auto-rejecting destinations.
   - Zero database footprint (no JPA, Hibernate, or SQL dependencies).

3. **Google Places API Integration & Photo Proxy**:
   - Searches Google Places for verified destination details (address, coordinates, star ratings, review count).
   - Documented photo retrieval flow via `/api/places/photo` proxy.
   - Shows required photo author attribution.
   - Fallback SVG image and explanatory badges when a photo is unavailable or destination is unmatched.
   - Direct Google Maps link.

4. **Modern Responsive Frontend**:
   - Clean UI crafted with modern CSS (custom properties, responsive grid, flexbox).
   - Vanilla JavaScript (no external libraries or frameworks).
   - Shows user's target budget side-by-side with estimates without auto-rejecting destinations.
   - Collapsible day-by-day itineraries and cost assumptions.

---

## Configuration & Environment Variables

Set your server-side API keys via environment variables or in `src/main/resources/application.properties`:

### Environment Variables
```bash
# Gemini API Key (Required for AI recommendations)
export GEMINI_API_KEY="your-gemini-api-key"

# Google Places API Key (Optional, for real photos and ratings)
export GOOGLE_PLACES_API_KEY="your-google-places-api-key"
```

### `application.properties`
```properties
gemini.api.key=${GEMINI_API_KEY:}
gemini.api.model=gemini-1.5-flash
google.places.api.key=${GOOGLE_PLACES_API_KEY:}
```

> **Note**: If `GOOGLE_PLACES_API_KEY` is omitted, the application operates in graceful fallback mode: real destination queries fall back to direct Google Maps search links and elegant placeholder destination artwork with an informative notice.

---

## Running the Application

### 1. Build and Run via Maven
```bash
mvn clean spring-boot:run
```
Or using the Maven Wrapper:
```bash
./mvnw spring-boot:run      # Linux/macOS
mvnw.cmd spring-boot:run    # Windows
```

### 2. Access the Web Interface
Open your browser and navigate to:
```
http://localhost:8080
```

---

## Running Automated Tests

Run the complete test suite including mocked external API responses:
```bash
mvn test
```

### Test Coverage Highlights
- `GeminiServiceTest`: Mocks Gemini responses, validates JSON parsing, handles markdown code fences, detects missing fields, and tests error handling for 500/network failures and missing API keys.
- `GooglePlacesServiceTest`: Tests Places API (New) & Legacy Text Search, photo proxying, author attribution extraction, missing API key fallback, and unmatched destinations.
- `TravelRecommendationServiceTest`: Tests end-to-end orchestration, ensuring cost estimates and user preferences remain intact and unadulterated.
- `TravelRecommendationControllerTest`: Tests input validation, 400 Bad Request error details, 502/401 exception handling, and `/api/places/photo` image streaming with Cache-Control headers.
