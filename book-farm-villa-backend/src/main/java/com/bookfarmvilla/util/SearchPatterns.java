package com.bookfarmvilla.util;

import java.util.Locale;

public final class SearchPatterns {

    private SearchPatterns() {
    }

    /**
     * Returns null for a blank keyword, otherwise a lower-case "%keyword%" LIKE
     * pattern.
     */
    public static String contains(String keyword) {
        if (keyword == null || keyword.isBlank())
            return null;
        return "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
    }
}