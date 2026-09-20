package com.bookfarmvilla.dto.request;

import com.bookfarmvilla.entity.enums.LeadStatus;
import jakarta.validation.constraints.NotNull;

public record LeadStatusUpdateRequest(
        @NotNull(message = "Status is required")
        LeadStatus status
) {
}
