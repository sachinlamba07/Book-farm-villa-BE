package com.bookfarmvilla.dto.response;

import java.math.BigDecimal;

public record BookingLineItemResponse(
        String description,
        String quantity,
        BigDecimal rate,
        BigDecimal amount
) {
}
