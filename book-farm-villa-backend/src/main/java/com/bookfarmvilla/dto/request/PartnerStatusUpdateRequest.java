package com.bookfarmvilla.dto.request;

import com.bookfarmvilla.entity.enums.PartnerStatus;
import jakarta.validation.constraints.NotNull;

public record PartnerStatusUpdateRequest(
        @NotNull(message = "Status is required")
        PartnerStatus status
) {
}
