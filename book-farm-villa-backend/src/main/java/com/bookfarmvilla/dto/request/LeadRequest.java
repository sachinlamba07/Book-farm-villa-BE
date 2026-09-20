package com.bookfarmvilla.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Public "Request a Callback" submission. Exactly one of propertyId / vendorId
 * should be provided - validated in the service layer.
 */
public record LeadRequest(
        String id,
        String propertyId,
        String vendorId,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Phone is required")
        String phone,

        LocalDate preferredDate,
        String eventType,
        String message
) {
}
