package com.bookfarmvilla.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Public "Partner with us" submission. Status is always set server-side to
 * PENDING on creation - it is not accepted from the client.
 */
public record PartnerRequest(
        String id,

        @NotBlank(message = "Business name is required")
        String businessName,

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Business type is required")
        String businessType,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Location is required")
        String location,

        @NotBlank(message = "Phone number is required")
        String phoneNumber
) {
}
