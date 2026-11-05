package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.ReviewStatus;

import java.util.List;
import java.time.Instant;

public record ReviewResponse(
        String id,
        String propertyId,
        String propertyTitle,
        String vendorId,
        String vendorName,
        String reviewerName,
        Integer rating,
        String title,
        String comment,
        String bookingReference,
        List<String> mediaUrls,
        ReviewStatus status,
        Instant createdAt
) {
}
