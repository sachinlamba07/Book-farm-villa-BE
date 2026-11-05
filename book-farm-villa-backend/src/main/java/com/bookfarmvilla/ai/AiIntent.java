package com.bookfarmvilla.ai;

import java.util.Locale;
import java.util.Set;

public record AiIntent(
        String intent,
        AiRequirements requirements,
        String response
) {
    public AiIntent {
        if (intent == null || intent.isBlank()) intent = "GENERAL";
        intent = intent.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("SEARCH_PROPERTY", "SEARCH_VENDOR", "SEARCH_BOTH", "PROPERTY_QUESTION", "COMPARE",
                "BOOK_ENQUIRY", "GENERAL", "OFF_TOPIC").contains(intent)) {
            intent = "GENERAL";
        }
        if (requirements == null) requirements = AiRequirements.empty();
        if (response == null) response = "";
    }

}
