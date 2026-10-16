package com.bookfarmvilla.dto.request;

import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.VendorCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VendorRequest(
        String id,

        @Size(max = 255, message = "Slug must be at most 255 characters")
        String slug,

        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Category is required")
        VendorCategory category,

        String description,

        @NotBlank(message = "City is required")
        String city,

        String priceRange,

        @NotBlank(message = "Contact phone is required")
        String contactPhone,

        ListingStatus status,

        List<String> imageUrls
) {
}
