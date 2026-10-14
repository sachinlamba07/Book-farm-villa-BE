package com.bookfarmvilla.dto.response;

import java.time.Instant;

public record BlogPostSummaryResponse(
        String id,
        String slug,
        String title,
        String excerpt,
        String coverImageUrl,
        String coverImageAlt,
        String category,
        String authorName,
        Instant publishedAt
) {
}
