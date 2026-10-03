package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.BookingStatus;
import com.bookfarmvilla.entity.enums.PaymentStatus;
import com.bookfarmvilla.entity.enums.PropertyType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BookingResponse(
        String id,
        String customerName,
        String customerPhone,
        String propertyId,
        String propertyTitle,
        PropertyType listingType,
        LocalDate bookingDate,
        BigDecimal amount,
        BigDecimal advanceAmount,
        BigDecimal commissionPercentage,
        BigDecimal commissionAmount,
        BookingStatus status,
        PaymentStatus paymentStatus,
        Instant createdAt,
        Instant modifiedAt
) {
}
