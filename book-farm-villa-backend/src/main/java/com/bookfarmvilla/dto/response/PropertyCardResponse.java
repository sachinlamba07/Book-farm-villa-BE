package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.PropertyType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record PropertyCardResponse(
        String id,
        String slug,
        String title,
        PropertyType type,
        Double latitude,
        Double longitude,
        String city,
        String address,
        Integer capacity,
        Set<String> amenities,
        BigDecimal startingPrice,
        BigDecimal endingPrice,
        Double averageRating,
        Long reviewCount,
        List<String> imageUrls
) {
}
