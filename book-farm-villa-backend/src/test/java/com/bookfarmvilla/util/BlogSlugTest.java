package com.bookfarmvilla.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlogSlugTest {

    @Test
    void buildsUrlFriendlySlugFromTitle() {
        assertEquals("best-dandiya-garba-nights-delhi-ncr-2026",
                BlogSlug.fromTitle("Best Dandiya & Garba Nights in Delhi NCR 2026"));
    }

    @Test
    void removesDiacriticsAndUsesFallbackForNonLatinTitles() {
        assertEquals("cafe-farm", BlogSlug.fromTitle("Café Farm"));
        assertEquals("blog-post", BlogSlug.fromTitle("हिंदी ब्लॉग"));
    }

    @Test
    void limitsGeneratedAndSuffixedSlugsToDatabaseColumnLength() {
        String slug = BlogSlug.fromTitle("a".repeat(300));

        assertEquals(255, slug.length());
        assertEquals(255, BlogSlug.withSuffix(slug, 2).length());
    }
}
