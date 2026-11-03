package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.BookingStatus;
import com.bookfarmvilla.entity.enums.PaymentStatus;
import com.bookfarmvilla.entity.enums.PropertyType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record BookingResponse(
        String id,
        String customerName,
        String customerPhone,
        String customerEmail,
        String propertyId,
        String propertyTitle,
        PropertyType listingType,
        LocalDate bookingDate,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        LocalTime startTime,
        LocalTime endTime,
        String invoiceNumber,
        String gstin,
        String placeOfSupply,
        List<BookingLineItemResponse> lineItems,
        BigDecimal amount,
        BigDecimal advanceAmount,
        BigDecimal commissionPercentage,
        BigDecimal commissionAmount,
        BigDecimal discountAmount,
        BigDecimal gstAmount,
        String paymentMode,
        String transactionReference,
        String notes,
        BookingStatus status,
        PaymentStatus paymentStatus,
        Instant createdAt,
        Instant modifiedAt
) {
}
