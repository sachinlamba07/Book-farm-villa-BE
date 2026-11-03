package com.bookfarmvilla.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BookingLineItemRequest(
        @NotBlank(message = "Service description is required")
        @Size(max = 255, message = "Service description must be 255 characters or fewer")
        String description,

        @NotBlank(message = "Service quantity is required")
        @Size(max = 80, message = "Service quantity must be 80 characters or fewer")
        String quantity,

        @NotNull(message = "Service rate is required")
        @DecimalMin(value = "0.00", message = "Service rate must be zero or more")
        BigDecimal rate,

        @NotNull(message = "Service amount is required")
        @DecimalMin(value = "0.00", message = "Service amount must be zero or more")
        BigDecimal amount
) {
}
