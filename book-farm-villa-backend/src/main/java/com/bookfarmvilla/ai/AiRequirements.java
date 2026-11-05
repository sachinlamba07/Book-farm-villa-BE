package com.bookfarmvilla.ai;

import java.util.List;

public record AiRequirements(
        String location,
        Integer guests,
        Integer budget,
        Integer budgetMin,
        Integer budgetMax,
        String propertyType,
        List<String> amenities,
        String purpose,
        String date,
        List<String> vendorCategories,
        List<String> excludedTypes
) {
    public AiRequirements(
            String location,
            Integer guests,
            Integer budget,
            String propertyType,
            List<String> amenities,
            String purpose,
            String date
    ) {
        this(location, guests, budget, null, null, propertyType, amenities, purpose, date, List.of(), List.of());
    }

    public AiRequirements(
            String location,
            Integer guests,
            Integer budget,
            String propertyType,
            List<String> amenities,
            String purpose,
            String date,
            List<String> excludedTypes
    ) {
        this(location, guests, budget, null, null, propertyType, amenities, purpose, date, List.of(), excludedTypes);
    }

    public AiRequirements(
            String location,
            Integer guests,
            Integer budget,
            String propertyType,
            List<String> amenities,
            String purpose,
            String date,
            List<String> vendorCategories,
            List<String> excludedTypes
    ) {
        this(location, guests, budget, null, null, propertyType, amenities, purpose, date, vendorCategories, excludedTypes);
    }

    public AiRequirements {
        amenities = amenities == null
                ? List.of()
                : amenities.stream().filter(value -> value != null).toList();
        vendorCategories = vendorCategories == null
                ? List.of()
                : vendorCategories.stream().filter(value -> value != null).toList();
        excludedTypes = excludedTypes == null
                ? List.of()
                : excludedTypes.stream().filter(value -> value != null).toList();
    }

    public static AiRequirements empty() {
        return new AiRequirements(null, null, null, null, null, null, List.of(), null, null, List.of(), List.of());
    }
}
