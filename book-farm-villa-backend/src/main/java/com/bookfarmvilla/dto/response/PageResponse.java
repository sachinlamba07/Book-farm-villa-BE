package com.bookfarmvilla.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }

    /** Transforms each item, keeping the paging info. */
    public <R> PageResponse<R> map(Function<T, R> mapper) {
        return new PageResponse<>(
                content.stream().map(mapper).toList(),
                pageNumber, pageSize, totalElements, totalPages, last);
    }
}