package com.bookfarmvilla.util;

import java.text.Normalizer;
import java.util.Locale;

public final class PropertySlug {

    private static final int MAX_LENGTH = 255;

    private PropertySlug() {
    }

    public static String from(String title, String city) {
        String value = Normalizer.normalize(title + "-" + city, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (value.length() > MAX_LENGTH) {
            value = value.substring(0, MAX_LENGTH).replaceAll("-+$", "");
        }
        return value.isEmpty() ? "property" : value;
    }

    public static String withSuffix(String slug, int suffix) {
        String suffixText = "-" + suffix;
        int baseLength = MAX_LENGTH - suffixText.length();
        String base = slug.substring(0, Math.min(slug.length(), baseLength)).replaceAll("-+$", "");
        return base + suffixText;
    }
}
