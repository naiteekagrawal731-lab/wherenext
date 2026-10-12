package com.travel.recommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.recommendation.exception.GooglePlacesApiException;
import com.travel.recommendation.model.PhotoAttributionDto;
import com.travel.recommendation.model.PlaceDetailsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GooglePlacesService {

    private static final Logger log = LoggerFactory.getLogger(GooglePlacesService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${google.places.api.key:}")
    private String apiKey;

    @Value("${google.places.api.url:https://places.googleapis.com/v1}")
    private String placesNewApiUrl;

    @Value("${google.places.legacy.url:https://maps.googleapis.com/maps/api/place}")
    private String placesLegacyApiUrl;

    public GooglePlacesService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public PlaceDetailsDto getPlaceDetailsForDestination(String destinationName, String state) {
        PlaceDetailsDto details = new PlaceDetailsDto();
        String query = (destinationName != null ? destinationName.trim() : "") +
                (state != null && !state.trim().isEmpty() ? ", " + state.trim() : "") +
                ", India";

        String defaultMapsUrl = "https://www.google.com/maps/search/?api=1&query=" +
                URLEncoder.encode(query, StandardCharsets.UTF_8);
        details.setGoogleMapsUrl(defaultMapsUrl);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.info("Google Places API key is not configured; generating fallback details for query: {}", query);
            details.setFormattedAddress(destinationName + (state != null ? ", " + state : "") + ", India");
            details.setPhotoAvailable(false);
            details.setPhotoUnavailableReason("Google Places API key is not configured on the server.");
            return details;
        }

        try {
            // 1. Try Google Places API (New) Text Search
            boolean matched = searchPlacesNew(query, details);

            // 2. If not matched, try legacy Text Search API as fallback
            if (!matched) {
                matched = searchPlacesLegacy(query, details);
            }

            if (!matched) {
                log.warn("Destination '{}' could not be matched in Google Places", query);
                details.setPhotoAvailable(false);
                details.setPhotoUnavailableReason("Destination could not be matched in Google Places.");
            }
        } catch (Exception ex) {
            log.error("Error retrieving Google Places details for '{}': {}", query, ex.getMessage(), ex);
            details.setPhotoAvailable(false);
            details.setPhotoUnavailableReason("Error communicating with Google Places API: " + ex.getMessage());
        }

        return details;
    }

    private boolean searchPlacesNew(String query, PlaceDetailsDto details) {
        try {
            String url = placesNewApiUrl + "/places:searchText";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Goog-Api-Key", apiKey.trim());
            headers.set("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.location,places.rating,places.userRatingCount,places.googleMapsUri,places.photos");

            Map<String, Object> body = Collections.singletonMap("textQuery", query);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return false;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode places = root.path("places");
            if (!places.isArray() || places.isEmpty()) {
                return false;
            }

            JsonNode firstPlace = places.get(0);
            details.setPlaceId(firstPlace.path("id").asText(null));
            details.setFormattedAddress(firstPlace.path("formattedAddress").asText(null));

            if (firstPlace.has("location")) {
                details.setLatitude(firstPlace.path("location").path("latitude").asDouble());
                details.setLongitude(firstPlace.path("location").path("longitude").asDouble());
            }

            if (firstPlace.has("rating")) {
                details.setRating(firstPlace.path("rating").asDouble());
            }
            if (firstPlace.has("userRatingCount")) {
                details.setUserRatingCount(firstPlace.path("userRatingCount").asInt());
            }
            if (firstPlace.has("googleMapsUri") && !firstPlace.path("googleMapsUri").asText().isEmpty()) {
                details.setGoogleMapsUrl(firstPlace.path("googleMapsUri").asText());
            }

            JsonNode photos = firstPlace.path("photos");
            if (photos.isArray() && !photos.isEmpty()) {
                JsonNode firstPhoto = photos.get(0);
                String photoResourceName = firstPhoto.path("name").asText(null);
                if (photoResourceName != null && !photoResourceName.isEmpty()) {
                    String proxyUrl = "/api/places/photo?name=" + URLEncoder.encode(photoResourceName, StandardCharsets.UTF_8);
                    details.setPhotoUrl(proxyUrl);
                    details.setPhotoAvailable(true);

                    JsonNode attributions = firstPhoto.path("authorAttributions");
                    if (attributions.isArray()) {
                        for (JsonNode attr : attributions) {
                            String displayName = attr.path("displayName").asText("Google Places Contributor");
                            String uri = attr.path("uri").asText(null);
                            details.getPhotoAttributions().add(new PhotoAttributionDto(displayName, uri));
                        }
                    }
                }
            } else {
                details.setPhotoAvailable(false);
                details.setPhotoUnavailableReason("No photos available in Google Places for this location.");
            }

            return true;
        } catch (Exception ex) {
            log.debug("searchPlacesNew attempt failed or skipped: {}", ex.getMessage());
            return false;
        }
    }

    private boolean searchPlacesLegacy(String query, PlaceDetailsDto details) {
        try {
            String url = placesLegacyApiUrl + "/textsearch/json?query=" +
                    URLEncoder.encode(query, StandardCharsets.UTF_8) +
                    "&key=" + apiKey.trim();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return false;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode results = root.path("results");
            if (!results.isArray() || results.isEmpty()) {
                return false;
            }

            JsonNode firstPlace = results.get(0);
            details.setPlaceId(firstPlace.path("place_id").asText(null));
            details.setFormattedAddress(firstPlace.path("formatted_address").asText(null));

            if (firstPlace.has("geometry") && firstPlace.path("geometry").has("location")) {
                JsonNode loc = firstPlace.path("geometry").path("location");
                details.setLatitude(loc.path("lat").asDouble());
                details.setLongitude(loc.path("lng").asDouble());
            }

            if (firstPlace.has("rating")) {
                details.setRating(firstPlace.path("rating").asDouble());
            }
            if (firstPlace.has("user_ratings_total")) {
                details.setUserRatingCount(firstPlace.path("user_ratings_total").asInt());
            }

            JsonNode photos = firstPlace.path("photos");
            if (photos.isArray() && !photos.isEmpty()) {
                JsonNode firstPhoto = photos.get(0);
                String photoRef = firstPhoto.path("photo_reference").asText(null);
                if (photoRef != null && !photoRef.isEmpty()) {
                    String proxyUrl = "/api/places/photo?photoReference=" + URLEncoder.encode(photoRef, StandardCharsets.UTF_8);
                    details.setPhotoUrl(proxyUrl);
                    details.setPhotoAvailable(true);

                    JsonNode htmlAttrs = firstPhoto.path("html_attributions");
                    if (htmlAttrs.isArray()) {
                        for (JsonNode attr : htmlAttrs) {
                            String text = attr.asText();
                            // parse anchor tag or plain text
                            String cleanName = text.replaceAll("<[^>]*>", "").trim();
                            details.getPhotoAttributions().add(new PhotoAttributionDto(cleanName, null));
                        }
                    }
                }
            } else {
                details.setPhotoAvailable(false);
                details.setPhotoUnavailableReason("No photos available in Google Places for this location.");
            }

            return true;
        } catch (Exception ex) {
            log.warn("searchPlacesLegacy attempt failed: {}", ex.getMessage());
            return false;
        }
    }

    public byte[] fetchPhotoBytes(String name, String photoReference, Integer maxWidth, Integer maxHeight) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new GooglePlacesApiException("Google Places API key is not configured.", HttpStatus.UNAUTHORIZED.value());
        }

        boolean hasName = (name != null && !name.trim().isEmpty());
        boolean hasRef = (photoReference != null && !photoReference.trim().isEmpty());
        if (!hasName && !hasRef) {
            throw new GooglePlacesApiException("Neither photo 'name' nor 'photoReference' parameter was provided.", HttpStatus.BAD_REQUEST.value());
        }

        int width = (maxWidth != null && maxWidth > 0) ? Math.min(maxWidth, 1600) : 800;
        int height = (maxHeight != null && maxHeight > 0) ? Math.min(maxHeight, 1200) : 600;

        try {
            if (hasName) {
                // Places API (New) media endpoint
                String url = placesNewApiUrl + "/" + name.trim() + "/media?maxWidthPx=" + width +
                        "&maxHeightPx=" + height +
                        "&key=" + apiKey.trim();

                ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } else {
                // Places Legacy Photo endpoint
                String url = placesLegacyApiUrl + "/photo?maxwidth=" + width +
                        "&photo_reference=" + photoReference.trim() +
                        "&key=" + apiKey.trim();

                ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            }
        } catch (GooglePlacesApiException ex) {
            throw ex;
        } catch (HttpStatusCodeException ex) {
            log.error("Google Places Photo HTTP error {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new GooglePlacesApiException("Failed to fetch photo from Google Places: " + ex.getStatusText(), ex.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Failed to fetch photo from Google Places: {}", ex.getMessage(), ex);
            throw new GooglePlacesApiException("Failed to fetch photo: " + ex.getMessage(), ex);
        }

        throw new GooglePlacesApiException("Google Places photo could not be retrieved.", HttpStatus.NOT_FOUND.value());
    }
}
