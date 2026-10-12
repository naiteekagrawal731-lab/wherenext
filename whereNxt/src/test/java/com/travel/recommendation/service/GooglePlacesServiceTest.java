package com.travel.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.recommendation.exception.GooglePlacesApiException;
import com.travel.recommendation.model.PlaceDetailsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GooglePlacesServiceTest {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private GooglePlacesService googlePlacesService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        googlePlacesService = new GooglePlacesService(restTemplate, objectMapper);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void getPlaceDetails_WhenApiKeyMissing_ReturnsSafeFallback() {
        ReflectionTestUtils.setField(googlePlacesService, "apiKey", "");

        PlaceDetailsDto details = googlePlacesService.getPlaceDetailsForDestination("Jaipur", "Rajasthan");

        assertNotNull(details);
        assertFalse(details.isPhotoAvailable());
        assertTrue(details.getPhotoUnavailableReason().contains("Google Places API key is not configured"));
        assertTrue(details.getGoogleMapsUrl().contains("google.com/maps/search"));
    }

    @Test
    void getPlaceDetails_WhenPlacesNewMatched_ReturnsDetailsAndProxyPhoto() {
        ReflectionTestUtils.setField(googlePlacesService, "apiKey", "test-places-key");
        ReflectionTestUtils.setField(googlePlacesService, "placesNewApiUrl", "https://places.googleapis.com/v1");

        String newApiResponse = """
            {
              "places": [
                {
                  "id": "ChIJbU60qSXDyjsRrzniMwMrnv8",
                  "formattedAddress": "Jaipur, Rajasthan, India",
                  "location": {
                    "latitude": 26.9124,
                    "longitude": 75.7873
                  },
                  "rating": 4.6,
                  "userRatingCount": 35000,
                  "googleMapsUri": "https://maps.google.com/?cid=12345",
                  "photos": [
                    {
                      "name": "places/ChIJbU60qSXDyjsRrzniMwMrnv8/photos/AUc7tXX12345",
                      "authorAttributions": [
                        {
                          "displayName": "Ravi Kumar",
                          "uri": "https://maps.google.com/contrib/ravi"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """;

        mockServer.expect(requestTo("https://places.googleapis.com/v1/places:searchText"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Goog-Api-Key", "test-places-key"))
                .andRespond(withSuccess(newApiResponse, MediaType.APPLICATION_JSON));

        PlaceDetailsDto details = googlePlacesService.getPlaceDetailsForDestination("Jaipur", "Rajasthan");
        mockServer.verify();

        assertNotNull(details);
        assertEquals("ChIJbU60qSXDyjsRrzniMwMrnv8", details.getPlaceId());
        assertEquals("Jaipur, Rajasthan, India", details.getFormattedAddress());
        assertEquals(26.9124, details.getLatitude());
        assertEquals(75.7873, details.getLongitude());
        assertEquals(4.6, details.getRating());
        assertEquals(35000, details.getUserRatingCount());
        assertEquals("https://maps.google.com/?cid=12345", details.getGoogleMapsUrl());
        assertTrue(details.isPhotoAvailable());
        assertTrue(details.getPhotoUrl().startsWith("/api/places/photo?name="));
        assertEquals(1, details.getPhotoAttributions().size());
        assertEquals("Ravi Kumar", details.getPhotoAttributions().get(0).getAuthorName());
    }

    @Test
    void getPlaceDetails_WhenUnmatchedInPlaces_ReturnsPhotoUnavailableNotice() {
        ReflectionTestUtils.setField(googlePlacesService, "apiKey", "test-places-key");
        ReflectionTestUtils.setField(googlePlacesService, "placesNewApiUrl", "https://places.googleapis.com/v1");
        ReflectionTestUtils.setField(googlePlacesService, "placesLegacyApiUrl", "https://maps.googleapis.com/maps/api/place");

        mockServer.expect(requestTo("https://places.googleapis.com/v1/places:searchText"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"places\": []}", MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(org.hamcrest.Matchers.startsWith("https://maps.googleapis.com/maps/api/place/textsearch/json")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"results\": []}", MediaType.APPLICATION_JSON));

        PlaceDetailsDto details = googlePlacesService.getPlaceDetailsForDestination("NonExistentCityXYZ", "Nowhere");
        mockServer.verify();

        assertNotNull(details);
        assertFalse(details.isPhotoAvailable());
        assertEquals("Destination could not be matched in Google Places.", details.getPhotoUnavailableReason());
        assertNotNull(details.getGoogleMapsUrl());
    }

    @Test
    void fetchPhotoBytes_WhenPhotoNameProvided_ReturnsImageBytes() {
        ReflectionTestUtils.setField(googlePlacesService, "apiKey", "test-places-key");
        ReflectionTestUtils.setField(googlePlacesService, "placesNewApiUrl", "https://places.googleapis.com/v1");

        byte[] fakeImage = new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };

        mockServer.expect(requestTo("https://places.googleapis.com/v1/places/123/photos/456/media?maxWidthPx=800&maxHeightPx=600&key=test-places-key"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(fakeImage, MediaType.IMAGE_JPEG));

        byte[] result = googlePlacesService.fetchPhotoBytes("places/123/photos/456", null, 800, 600);
        mockServer.verify();

        assertArrayEquals(fakeImage, result);
    }

    @Test
    void fetchPhotoBytes_WhenNeitherParamProvided_ThrowsGooglePlacesApiException() {
        ReflectionTestUtils.setField(googlePlacesService, "apiKey", "test-places-key");

        GooglePlacesApiException ex = assertThrows(GooglePlacesApiException.class, () ->
                googlePlacesService.fetchPhotoBytes(null, null, 800, 600)
        );
        assertEquals(400, ex.getStatusCode());
    }
}
