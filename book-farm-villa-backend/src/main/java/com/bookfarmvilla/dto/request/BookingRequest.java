package com.bookfarmvilla.dto.request;

import com.bookfarmvilla.entity.enums.BookingStatus;
import com.bookfarmvilla.entity.enums.PaymentStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingRequest(
        String id,

        @NotBlank(message = "Customer name is required")
        String customerName,

        String customerPhone,

        /** Optional - link this booking to an actual property listing. */
        String propertyId,

        @NotNull(message = "Listing type is required")
        PropertyType listingType,

        @NotNull(message = "Booking date is required")
        LocalDate bookingDate,

        @NotNull(message = "Amount is required")
        @PositiveOrZero(message = "Amount must be zero or positive")
        BigDecimal amount,

        BookingStatus status,

        PaymentStatus paymentStatus
) {
}
