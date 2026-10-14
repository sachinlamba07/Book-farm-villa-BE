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
 * Accepts an optional client-supplied id and slug. If the slug is null/blank, the
 * backend generates it from the title and city.
 */
public record PropertyRequest(
        String id,

        @Size(max = 255, message = "Slug must be at most 255 characters")
        String slug,

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
