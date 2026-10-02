package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.PartnerStatus;

import java.time.Instant;

public record PartnerResponse(
        String id,
        String businessName,
        String fullName,
        String businessType,
        String city,
        String location,
        String phoneNumber,
        PartnerStatus status,
        Instant createdAt,
        Instant modifiedAt
) {
}
