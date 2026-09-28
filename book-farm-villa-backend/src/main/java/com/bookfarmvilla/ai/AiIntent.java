package com.bookfarmvilla.ai;

import java.util.List;

public record AiIntent(
        String intent,
        AiRequirements requirements,
        String response
) {
    public AiIntent {
        if (requirements == null) requirements = AiRequirements.empty();
        if (response == null) response = "";
    }
}
