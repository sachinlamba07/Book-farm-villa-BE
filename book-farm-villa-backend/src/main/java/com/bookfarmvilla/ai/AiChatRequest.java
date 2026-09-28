package com.bookfarmvilla.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AiChatRequest(
        @NotBlank @Size(max = 1200) String message,
        @Size(max = 10) List<AiChatMessage> history,
        String propertyId,
        String vendorId,
        Double latitude,
        Double longitude
) {
}
