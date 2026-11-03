package com.bookfarmvilla.dto.request;

import com.bookfarmvilla.entity.enums.BookingStatus;
import com.bookfarmvilla.entity.enums.PaymentStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record BookingRequest(
        String id,

        @NotBlank(message = "Customer name is required")
        String customerName,

        String customerPhone,

        @Email(message = "Enter a valid customer email address")
        @Size(max = 255, message = "Customer email must be 255 characters or fewer")
        String customerEmail,

        /** Optional - link this booking to an actual property listing. */
        String propertyId,

        @NotNull(message = "Listing type is required")
        PropertyType listingType,

        @NotNull(message = "Booking date is required")
        LocalDate bookingDate,

        LocalDate checkInDate,

        LocalDate checkOutDate,

        LocalTime startTime,

        LocalTime endTime,

        @Size(max = 80, message = "Invoice number must be 80 characters or fewer")
        String invoiceNumber,

        @Size(max = 32, message = "GSTIN must be 32 characters or fewer")
        String gstin,

        @Size(max = 120, message = "Place of supply must be 120 characters or fewer")
        String placeOfSupply,

        @Size(max = 10, message = "A booking can have at most 10 service line items")
        List<@jakarta.validation.Valid BookingLineItemRequest> lineItems,

        @NotNull(message = "Amount is required")
        @PositiveOrZero(message = "Amount must be zero or positive")
        BigDecimal amount,

        @PositiveOrZero(message = "Advance amount must be zero or positive")
        BigDecimal advanceAmount,

        @PositiveOrZero(message = "Commission percentage must be zero or positive")
        @DecimalMax(value = "100.00", message = "Commission percentage must not exceed 100")
        BigDecimal commissionPercentage,

        @PositiveOrZero(message = "Commission amount must be zero or positive")
        BigDecimal commissionAmount,

        @PositiveOrZero(message = "Discount amount must be zero or positive")
        BigDecimal discountAmount,

        @PositiveOrZero(message = "GST amount must be zero or positive")
        BigDecimal gstAmount,

        @Size(max = 40, message = "Payment mode must be 40 characters or fewer")
        String paymentMode,

        @Size(max = 120, message = "Transaction reference must be 120 characters or fewer")
        String transactionReference,

        @Size(max = 2000, message = "Notes must be 2000 characters or fewer")
        String notes,

        BookingStatus status,

        PaymentStatus paymentStatus
) {
}
