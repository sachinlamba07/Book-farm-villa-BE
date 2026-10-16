package com.bookfarmvilla.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertySlugTest {

    @Test
    void buildsSlugFromTitleAndCity() {
        assertEquals("farmhouse-5252-chattarpur-delhi",
                PropertySlug.from("Farmhouse 5252", "Chattarpur, Delhi"));
    }

    @Test
    void removesDiacriticsAndPunctuation() {
        assertEquals("cafe-farm-delhi", PropertySlug.from("Café & Farm", "Delhi"));
    }

    @Test
    void usesFallbackWhenTitleAndCityHaveNoSlugCharacters() {
        assertEquals("property", PropertySlug.from("***", "!!!"));
    }

    @Test
    void keepsGeneratedAndSuffixedSlugsWithinColumnLimit() {
        String slug = PropertySlug.from("a".repeat(255), "Delhi");

        assertEquals(255, slug.length());
        assertEquals(255, PropertySlug.withSuffix(slug, 2).length());
    }
}
