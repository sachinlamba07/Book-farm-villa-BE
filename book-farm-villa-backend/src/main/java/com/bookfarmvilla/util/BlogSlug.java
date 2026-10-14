package com.bookfarmvilla.util;

import java.text.Normalizer;
import java.util.Locale;

public final class BlogSlug {

    private static final int MAX_LENGTH = 255;

    private BlogSlug() {
    }

    public static String fromTitle(String title) {
        String slug = normalize(title);
        return slug.isEmpty() ? "blog-post" : slug;
    }

    public static String normalize(String value) {
        String slug = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (slug.length() > MAX_LENGTH) {
            slug = slug.substring(0, MAX_LENGTH).replaceAll("-+$", "");
        }
        return slug;
    }

    public static String withSuffix(String slug, int suffix) {
        String suffixText = "-" + suffix;
        int baseLength = MAX_LENGTH - suffixText.length();
        String base = slug.substring(0, Math.min(slug.length(), baseLength)).replaceAll("-+$", "");
        return base + suffixText;
    }
}
