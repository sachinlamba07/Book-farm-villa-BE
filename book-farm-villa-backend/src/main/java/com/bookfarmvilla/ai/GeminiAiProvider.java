package com.bookfarmvilla.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class GeminiAiProvider implements AiProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiAiProvider(
            ObjectMapper objectMapper,
            @Value("${ai.gemini.api-key:}") String apiKey,
            @Value("${ai.gemini.model:gemini-3.5-flash-lite}") String model
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    @Override
    public String generate(String systemInstruction, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiProviderException("Gemini API key is not configured.");
        }

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of(
                        "parts", new Object[]{Map.of("text", systemInstruction)}
                ),
                "contents", new Object[]{
                        Map.of(
                                "role", "user",
                                "parts", new Object[]{Map.of("text", userPrompt)}
                        )
                },
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "maxOutputTokens", 600
                )
        );

        try {
            String raw = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            JsonNode text = root.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text");

            if (text.isMissingNode() || text.asText().isBlank()) {
                throw new AiProviderException("Gemini returned an empty response.");
            }

            return text.asText();
        } catch (Exception ex) {
            if (ex instanceof AiProviderException providerException) {
                throw providerException;
            }
            throw new AiProviderException("Gemini request failed.", ex);
        }
    }
}
