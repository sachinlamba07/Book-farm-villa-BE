package com.bookfarmvilla.dto.response;

import java.time.Instant;

public record ReviewResponse(
        String id,
        String propertyId,
        String vendorId,
        String reviewerName,
        Integer rating,
        String comment,
        Instant createdAt
) {
}
