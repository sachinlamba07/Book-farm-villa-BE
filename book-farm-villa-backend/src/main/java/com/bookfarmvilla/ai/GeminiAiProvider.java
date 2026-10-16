package com.bookfarmvilla.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Map;

@Component
public class GeminiAiProvider implements AiProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
        private final String fallbackModel;

    public GeminiAiProvider(
            ObjectMapper objectMapper,
            @Value("${ai.gemini.api-key:}") String apiKey,
                        @Value("${ai.gemini.model:gemini-3.1-flash-lite}") String model,
                        @Value("${ai.gemini.fallback-model:gemini-3.5-flash-lite}") String fallbackModel
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
                this.fallbackModel = fallbackModel;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
                requestFactory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public String generate(String systemInstruction, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiProviderException("Gemini API key is not configured.");
        }

                for (int attempt = 1; attempt <= 3; attempt++) {
                        try {
                                return generateWithModel(systemInstruction, userPrompt, model);
                        } catch (RestClientResponseException ex) {
                                if (ex.getStatusCode().value() != 503) {
                                        throw new AiProviderException("Gemini request failed with HTTP "
                                                        + ex.getStatusCode().value() + ".", ex);
                                }

                                System.err.println("[AI] Gemini returned HTTP 503 for model " + model
                                                + " (attempt " + attempt + "/3)");
                                if (attempt == 3) {
                                        break;
                                }
                                pauseBeforeRetry(attempt);
                        } catch (AiProviderException ex) {
                                throw ex;
                        } catch (Exception ex) {
                                throw new AiProviderException("Gemini request failed.", ex);
                        }
                }

                if (fallbackModel == null || fallbackModel.isBlank() || fallbackModel.equals(model)) {
                        throw new AiProviderException("Gemini returned HTTP 503 after three attempts; no distinct fallback model is configured.");
                }

                System.out.println("[AI] Switching to fallback Gemini model " + fallbackModel);
                try {
                        return generateWithModel(systemInstruction, userPrompt, fallbackModel);
                } catch (Exception ex) {
                        throw new AiProviderException("Gemini primary and fallback model requests failed.", ex);
                }
        }

        private String generateWithModel(String systemInstruction, String userPrompt, String requestModel) {
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
                        "maxOutputTokens", 300
                )
        );

        long startedAt = System.nanoTime();
        try {
            String raw = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/{model}:generateContent")
                            .build(requestModel))
                    .header("X-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            JsonNode text = root.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text");

            if (text.isMissingNode() || text.asText().isBlank()) {
                throw new AiProviderException("Gemini returned an empty response.");
            }

            System.out.println("[AI] Gemini request using " + requestModel + " completed in "
                    + elapsedMillis(startedAt) + " ms");
            return text.asText();
        } catch (Exception ex) {
            System.err.println("[AI] Gemini request using " + requestModel + " failed after "
                    + elapsedMillis(startedAt) + " ms.");
            if (ex instanceof RestClientResponseException responseException) {
                throw responseException;
            }
            if (ex instanceof AiProviderException providerException) {
                throw providerException;
            }
            throw new AiProviderException("Gemini request failed.", ex);
        }
    }

        private void pauseBeforeRetry(int attempt) {
                try {
                        Thread.sleep(500L * attempt);
                } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new AiProviderException("Interrupted while retrying Gemini request.", ex);
        }
        }

        private long elapsedMillis(long startedAt) {
                return (System.nanoTime() - startedAt) / 1_000_000;
        }
}
