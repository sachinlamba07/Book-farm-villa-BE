package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record PropertyV2Response(
        String id,
        String slug,
        String title,
        String originalPropertyName,
        String description,
        PropertyType type,
        String address,
        String city,
        Double latitude,
        Double longitude,
        java.math.BigDecimal startingPrice,
        java.math.BigDecimal endingPrice,
        Integer capacity,
        String contactPhone,
        ListingStatus status,
        Set<String> amenities,
        List<PropertyImageResponse> images,
        Double averageRating,
        Instant createdAt,
        Instant modifiedAt
) {
}
