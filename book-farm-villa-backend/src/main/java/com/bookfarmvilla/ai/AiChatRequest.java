package com.bookfarmvilla.ai;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AiChatRequest(
        @NotBlank @Size(max = 1200) String message,
        @Size(max = 10) List<AiChatMessage> history,
        @Size(max = 100) String propertyId,
        @Size(max = 100) String vendorId,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @Size(max = 50) List<@Size(max = 100) String> excludedPropertyIds,
        @Size(max = 50) List<@Size(max = 100) String> excludedVendorIds
) {
}
