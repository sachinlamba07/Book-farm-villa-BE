package com.bookfarmvilla.dto.request;

import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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

        String originalPropertyName,

        String description,

        @NotNull(message = "Property type is required")
        PropertyType type,

        String address,

        @NotBlank(message = "City is required")
        String city,

        Double latitude,

        Double longitude,

        @jakarta.validation.constraints.PositiveOrZero(message = "Starting price must be zero or positive")
        java.math.BigDecimal startingPrice,

        @jakarta.validation.constraints.PositiveOrZero(message = "Ending price must be zero or positive")
        java.math.BigDecimal endingPrice,

        @Positive(message = "Capacity must be positive")
        Integer capacity,

        @NotBlank(message = "Contact phone is required")
        String contactPhone,

        ListingStatus status,

        Set<String> amenityIds,

        List<@NotBlank(message = "Amenity names must not be blank")
                @Size(max = 100, message = "Amenity names must be at most 100 characters") String> amenities,

        List<String> imageUrls
) {
}
