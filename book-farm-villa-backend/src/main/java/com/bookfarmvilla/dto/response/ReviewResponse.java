package com.bookfarmvilla.dto.response;

import java.time.Instant;

public record ReviewResponse(
        String id,
        String propertyId,
        String propertyTitle,
        String vendorId,
        String vendorName,
        String reviewerName,
        Integer rating,
        String comment,
        Instant createdAt
) {
}
