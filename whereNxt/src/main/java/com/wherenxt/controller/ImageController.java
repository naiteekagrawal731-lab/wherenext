package com.wherenxt.controller;

import com.wherenxt.dto.ImageSearchResponse;
import com.wherenxt.service.WikimediaCommonsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    private final WikimediaCommonsService wikimediaService;

    public ImageController(WikimediaCommonsService wikimediaService) {
        this.wikimediaService = wikimediaService;
    }

    @GetMapping("/images")
    public ResponseEntity<ImageSearchResponse> searchImages(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Received image search request for query: {}, limit: {}", query, limit);
        ImageSearchResponse response = wikimediaService.searchImages(query, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/images/proxy")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        return wikimediaService.proxyImage(url);
    }
}
