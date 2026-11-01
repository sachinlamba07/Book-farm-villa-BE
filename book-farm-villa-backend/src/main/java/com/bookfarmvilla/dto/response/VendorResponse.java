package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.VendorCategory;

import java.time.Instant;
import java.util.List;

public record VendorResponse(
                String id,
                String slug,
                String name,
                VendorCategory category,
                String description,
                String city,
                String priceRange,
                String contactPhone,
                ListingStatus status,
                List<VendorImageResponse> images,
                Double averageRating,
                Instant createdAt,
                Instant modifiedAt) {
        /** Copy safe for anonymous visitors: the vendor's phone number is removed. */
        public VendorResponse publicView() {
                return new VendorResponse(
                                id, slug, name, category, description, city, priceRange, null, status,
                                images, averageRating, createdAt, modifiedAt);
        }
}