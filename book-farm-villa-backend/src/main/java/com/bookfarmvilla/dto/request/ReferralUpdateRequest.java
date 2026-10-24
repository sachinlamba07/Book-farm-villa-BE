package com.bookfarmvilla.dto.request;

import java.time.LocalDate;

/**
 * Admin update request for referral status, booking date, and booking amount.
 * All fields are optional - only provided fields will be updated.
 */
public record ReferralUpdateRequest(
        String status,
        LocalDate bookingDate,
        Double bookingAmount
) {
}