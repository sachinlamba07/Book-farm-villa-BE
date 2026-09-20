package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.VendorCategory;

import java.time.Instant;
import java.util.List;

public record VendorResponse(
        String id,
        String name,
        VendorCategory category,
        String description,
        String city,
        String priceRange,
        String contactPhone,
        ListingStatus status,
        List<VendorImageResponse> images,
        Double averageRating,
        Instant createdAt,
        Instant modifiedAt
) {
}
