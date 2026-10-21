package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.PropertyType;

import java.math.BigDecimal;

public record PropertyCardBaseRow(
        String id,
        String slug,
        String title,
        PropertyType type,
        Double latitude,
        Double longitude,
        String city,
        String address,
        Integer capacity,
        BigDecimal startingPrice,
        BigDecimal endingPrice,
        Double ratingAverage,
        Long ratingCount
) {
}
