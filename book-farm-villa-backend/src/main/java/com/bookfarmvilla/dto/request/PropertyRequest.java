package com.bookfarmvilla.dto.request;

import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Set;

/**
 * Accepts an optional client-supplied id. If null/blank, the backend generates one
 * (see BaseEntity#onCreate). This lets a future offline-first client generate its own
 * UUIDs while still working correctly when it doesn't.
 */
public record PropertyRequest(
        String id,

        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotNull(message = "Property type is required")
        PropertyType type,

        String address,

        @NotBlank(message = "City is required")
        String city,

        String priceRange,

        @Positive(message = "Capacity must be positive")
        Integer capacity,

        @NotBlank(message = "Contact phone is required")
        String contactPhone,

        ListingStatus status,

        Set<String> amenityIds,

        List<String> imageUrls
) {
}
