package com.bookfarmvilla.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Public referral submission - only customer mobile is required.
 * All other fields are optional and can be set later by admin.
 */
public record ReferralCreateRequest(
        @NotBlank(message = "Customer mobile number is required")
        String customerMobile,

        String referrerMobile,
        String event,
        String description,
        String city,
        String propertyId,
        LocalDate bookingDate
) {
}