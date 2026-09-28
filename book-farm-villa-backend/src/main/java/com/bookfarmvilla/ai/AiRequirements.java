package com.bookfarmvilla.ai;

import java.util.List;

public record AiRequirements(
        String location,
        Integer guests,
        Integer budget,
        String propertyType,
        List<String> amenities,
        String purpose,
        String date
) {
    public static AiRequirements empty() {
        return new AiRequirements(null, null, null, null, List.of(), null, null);
    }
}
