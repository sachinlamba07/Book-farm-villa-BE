package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.LeadStatus;

import java.time.Instant;
import java.time.LocalDate;

public record LeadResponse(
        String id,
        String propertyId,
        String propertyTitle,
        String originalPropertyName,
        String vendorId,
        String vendorName,
        String name,
        String phone,
        String email,
        LocalDate preferredDate,
        String eventType,
        String message,
        LeadStatus status,
        Instant createdAt,
        Instant modifiedAt
) {
}
