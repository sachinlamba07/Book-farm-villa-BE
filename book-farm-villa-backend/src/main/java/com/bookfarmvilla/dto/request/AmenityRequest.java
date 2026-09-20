package com.bookfarmvilla.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AmenityRequest(
        String id,

        @NotBlank(message = "Name is required")
        String name
) {
}
