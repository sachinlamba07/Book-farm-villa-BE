package com.bookfarmvilla.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PublicReviewRequest(
        @NotBlank(message = "Reviewer name is required")
        @Size(max = 100, message = "Reviewer name must be 100 characters or fewer")
        String reviewerName,

        @Size(max = 80, message = "Booking reference must be 80 characters or fewer")
        String bookingReference,

        String propertyId,

        String vendorId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5")
        Integer rating,

        @NotBlank(message = "Review title is required")
        @Size(max = 160, message = "Review title must be 160 characters or fewer")
        String title,

        @NotBlank(message = "Review text is required")
        @Size(max = 500, message = "Review must be 500 characters or fewer")
        String comment,

        @Size(max = 10, message = "A review can include at most 10 media files")
        List<@NotBlank @Size(max = 2048) String> mediaUrls
) {
}
