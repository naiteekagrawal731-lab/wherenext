package com.wherenxt.controller;

import com.wherenxt.dto.ImageMetadataDto;
import com.wherenxt.dto.ImageSearchResponse;
import com.wherenxt.service.WikimediaCommonsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WikimediaCommonsService wikimediaService;

    @Test
    @DisplayName("GET /api/images returns 200 OK with Wikimedia Commons image search results")
    void searchImages_returnsImageResults() throws Exception {
        ImageMetadataDto img = new ImageMetadataDto(
                "https://upload.wikimedia.org/wikipedia/commons/hawa_mahal.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/hawa_mahal.jpg",
                "File:Hawa Mahal",
                "Hawa Mahal in Jaipur",
                "Jakub Halun",
                "CC BY-SA 4.0",
                "https://creativecommons.org/licenses/by-sa/4.0",
                "https://commons.wikimedia.org/wiki/File:Hawa_Mahal.jpg",
                "Photo: Jakub Halun via Wikimedia Commons, CC BY-SA 4.0"
        );

        ImageSearchResponse mockResponse = new ImageSearchResponse("Jaipur", 1, List.of(img));
        when(wikimediaService.searchImages("Jaipur", 5)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/images")
                        .param("query", "Jaipur")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("Jaipur"))
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.images[0].artist").value("Jakub Halun"))
                .andExpect(jsonPath("$.images[0].license").value("CC BY-SA 4.0"))
                .andExpect(jsonPath("$.images[0].attributionText").value("Photo: Jakub Halun via Wikimedia Commons, CC BY-SA 4.0"));
    }
}
