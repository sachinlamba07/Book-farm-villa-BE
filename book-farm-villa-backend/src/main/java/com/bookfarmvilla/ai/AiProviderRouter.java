package com.bookfarmvilla.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiProviderRouter {

    private final String provider;
    private final AiProvider gemini;

    public AiProviderRouter(
            @Value("${ai.provider:gemini}") String provider,
            GeminiAiProvider gemini
    ) {
        this.provider = provider.trim().toLowerCase();
        this.gemini = gemini;
    }

    public AiProvider current() {
        return switch (provider) {
            case "gemini" -> gemini;
            default -> throw new AiProviderException(
                    "Unsupported AI_PROVIDER '" + provider + "'. Add a provider implementation before enabling it."
            );
        };
    }
}
