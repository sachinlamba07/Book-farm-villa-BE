package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.BlogPostStatus;

import java.time.Instant;

public record BlogPostResponse(
        String id,
        String slug,
        String title,
        String excerpt,
        String content,
        String coverImageUrl,
        String coverImageAlt,
        String category,
        String authorName,
        BlogPostStatus status,
        String seoTitle,
        String seoDescription,
        Instant publishedAt,
        Instant createdAt,
        Instant modifiedAt
) {
}
