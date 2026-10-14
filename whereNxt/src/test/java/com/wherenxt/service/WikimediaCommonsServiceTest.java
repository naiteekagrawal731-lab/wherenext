package com.wherenxt.service;

import com.wherenxt.config.WikimediaProperties;
import com.wherenxt.dto.ImageMetadataDto;
import com.wherenxt.dto.ImageSearchResponse;
import com.wherenxt.dto.wikimedia.WikimediaImageInfo;
import com.wherenxt.dto.wikimedia.WikimediaMetadataField;
import com.wherenxt.dto.wikimedia.WikimediaPage;
import com.wherenxt.dto.wikimedia.WikimediaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WikimediaCommonsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private WikimediaProperties properties;
    private WikimediaCommonsService service;

    @BeforeEach
    void setUp() {
        properties = new WikimediaProperties();
        properties.setBaseUrl("https://commons.wikimedia.org/w/api.php");
        properties.setUserAgent("whereNxt-TravelApp/1.0");
        properties.setDefaultThumbnailWidth(800);
        service = new WikimediaCommonsService(restTemplate, properties);
    }

    @Test
    @DisplayName("searchImages parses MediaWiki API response and cleans HTML in attribution")
    void searchImages_parsesResponseAndCleansHtml() {
        WikimediaResponse response = new WikimediaResponse();
        WikimediaResponse.WikimediaQuery query = new WikimediaResponse.WikimediaQuery();
        Map<String, WikimediaPage> pages = new HashMap<>();

        WikimediaPage page = new WikimediaPage();
        page.setPageid(94612098);
        page.setTitle("File:Hawa Mahal Palace.jpg");

        WikimediaImageInfo info = new WikimediaImageInfo();
        info.setUrl("https://upload.wikimedia.org/hawa_original.jpg");
        info.setThumburl("https://upload.wikimedia.org/hawa_800.jpg");
        info.setDescriptionurl("https://commons.wikimedia.org/wiki/File:Hawa_Mahal_Palace.jpg");

        Map<String, WikimediaMetadataField> ext = new HashMap<>();
        ext.put("Artist", new WikimediaMetadataField("<a href=\"//commons.wikimedia.org/wiki/User:Jakub\">Jakub Halun</a>"));
        ext.put("ImageDescription", new WikimediaMetadataField("<b>Hawa Mahal</b> in Jaipur"));
        ext.put("LicenseShortName", new WikimediaMetadataField("CC BY-SA 4.0"));
        ext.put("LicenseUrl", new WikimediaMetadataField("https://creativecommons.org/licenses/by-sa/4.0"));
        info.setExtmetadata(ext);

        page.setImageinfo(List.of(info));
        pages.put("94612098", page);
        query.setPages(pages);
        response.setQuery(query);

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(WikimediaResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        ImageSearchResponse result = service.searchImages("Jaipur", 1);

        assertThat(result.getTotalResults()).isEqualTo(1);
        ImageMetadataDto img = result.getImages().get(0);
        assertThat(img.getArtist()).isEqualTo("Jakub Halun"); // HTML stripped!
        assertThat(img.getDescription()).isEqualTo("Hawa Mahal in Jaipur"); // HTML stripped!
        assertThat(img.getLicense()).isEqualTo("CC BY-SA 4.0");
        assertThat(img.getThumbnailUrl()).isEqualTo("https://upload.wikimedia.org/hawa_800.jpg");
        assertThat(img.getAttributionText()).isEqualTo("Photo: Jakub Halun via Wikimedia Commons, licensed under CC BY-SA 4.0");
    }

    @Test
    @DisplayName("searchImages provides curated fallback when API fails or returns no results")
    void searchImages_whenApiFails_returnsCuratedFallback() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(WikimediaResponse.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        ImageSearchResponse result = service.searchImages("Alappuzha", 1);

        assertThat(result.getTotalResults()).isGreaterThanOrEqualTo(1);
        ImageMetadataDto fallback = result.getImages().get(0);
        assertThat(fallback.getUrl()).contains("wikimedia.org");
        assertThat(fallback.getArtist()).isNotEmpty();
        assertThat(fallback.getLicense()).isNotEmpty();
        assertThat(fallback.getAttributionText()).contains("Wikimedia Commons");
    }

    @Test
    @DisplayName("cleanHtml removes script/tags and decodes entities properly")
    void cleanHtml_handlesVariousFormats() {
        assertThat(WikimediaCommonsService.cleanHtml("<a href=\"link\">Author Name</a>")).isEqualTo("Author Name");
        assertThat(WikimediaCommonsService.cleanHtml("Hello &nbsp; <b>World</b> &amp; Peace")).isEqualTo("Hello World & Peace");
        assertThat(WikimediaCommonsService.cleanHtml(null)).isEmpty();
    }
}
