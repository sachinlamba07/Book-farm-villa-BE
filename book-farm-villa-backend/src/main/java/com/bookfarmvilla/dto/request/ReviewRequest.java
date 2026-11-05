package com.bookfarmvilla.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReviewRequest(
        String id,
        String propertyId,
        String vendorId,

        @NotBlank(message = "Reviewer name is required")
        String reviewerName,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5")
        Integer rating,

        String comment,

        @Size(max = 160, message = "Review title must be 160 characters or fewer")
        String title,

        @Size(max = 80, message = "Booking reference must be 80 characters or fewer")
        String bookingReference,

        @Size(max = 10, message = "A review can include at most 10 media files")
        List<@NotBlank @Size(max = 2048) String> mediaUrls
) {
}
