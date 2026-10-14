package com.bookfarmvilla.dto.response;

import com.bookfarmvilla.entity.enums.BlogPostStatus;

import java.time.Instant;

public record AdminBlogPostSummaryResponse(
        String id,
        String slug,
        String title,
        String excerpt,
        String coverImageUrl,
        String category,
        String authorName,
        BlogPostStatus status,
        Instant publishedAt,
        Instant createdAt,
        Instant modifiedAt
) {
}
