package com.bookfarmvilla.util;

import com.bookfarmvilla.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Locale;
import java.util.Set;

public final class ListSort {

    private ListSort() {
    }

    public static Pageable apply(Pageable pageable, String sortBy, String sortDirection, Set<String> allowedFields) {
        Sort.Direction direction = parseDirection(sortDirection);
        if (sortBy == null) {
            return pageable;
        }
        if (!allowedFields.contains(sortBy)) {
            String allowedValues = String.join(", ", allowedFields.stream().sorted().toList());
            throw new BadRequestException("Invalid sortBy '" + sortBy + "'. Allowed values: " + allowedValues);
        }

        Sort.Direction effectiveDirection = direction != null ? direction : Sort.Direction.ASC;
        Sort sort = Sort.by(
                new Sort.Order(effectiveDirection, sortBy).nullsLast(),
                Sort.Order.asc("id")
        );
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private static Sort.Direction parseDirection(String sortDirection) {
        if (sortDirection == null) {
            return null;
        }

        return switch (sortDirection.trim().toLowerCase(Locale.ROOT)) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new BadRequestException("Invalid sortDirection '" + sortDirection + "'. Allowed values: asc, desc");
        };
    }
}
