package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.PropertyType;

public record PropertyTypeCountRow(PropertyType type, Long count) {
}