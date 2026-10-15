package com.bookfarmvilla.dto.request;

import com.bookfarmvilla.entity.enums.BlogPostStatus;
import jakarta.validation.constraints.NotNull;

public record BlogPostStatusRequest(
        @NotNull(message = "Blog post status is required")
        BlogPostStatus status
) {
}
