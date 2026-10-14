package com.wherenxt.dto.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiGenerateRequest {

    private List<Content> contents = new ArrayList<>();
    private GenerationConfig generationConfig;

    public GeminiGenerateRequest() {
    }

    public static GeminiGenerateRequest of(String prompt) {
        GeminiGenerateRequest request = new GeminiGenerateRequest();
        Part part = new Part(prompt);
        Content content = new Content(Collections.singletonList(part));
        request.setContents(Collections.singletonList(content));
        
        GenerationConfig config = new GenerationConfig();
        config.setTemperature(0.7);
        config.setResponseMimeType("application/json");
        request.setGenerationConfig(config);
        
        return request;
    }

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }

    public GenerationConfig getGenerationConfig() {
        return generationConfig;
    }

    public void setGenerationConfig(GenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }

    public static class Content {
        private List<Part> parts = new ArrayList<>();

        public Content() {
        }

        public Content(List<Part> parts) {
            this.parts = parts;
        }

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        private String text;

        public Part() {
        }

        public Part(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class GenerationConfig {
        private Double temperature;
        private String responseMimeType;

        public GenerationConfig() {
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public String getResponseMimeType() {
            return responseMimeType;
        }

        public void setResponseMimeType(String responseMimeType) {
            this.responseMimeType = responseMimeType;
        }
    }
}
