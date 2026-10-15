package com.bookfarmvilla.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlogHtmlSanitizerTest {

    @Test
    void preservesEditorFormattingAndSafeStyles() {
        String result = BlogHtmlSanitizer.sanitize(
                "<h2>Heading</h2><p><span style=\"font-size: 20px; color: #123456\">Text</span></p>"
                        + "<a href=\"https://example.com\" target=\"_blank\">Link</a>"
        );

        assertTrue(result.contains("<h2>Heading</h2>"));
        assertTrue(result.contains("font-size: 20px"));
        assertTrue(result.contains("color: #123456"));
        assertTrue(result.contains("rel=\"noopener noreferrer\""));
    }

    @Test
    void removesScriptsEventHandlersUnsafeProtocolsAndUnsafeStyles() {
        String result = BlogHtmlSanitizer.sanitize(
                "<script>alert(1)</script><img src=\"x\" onerror=\"alert(1)\">"
                        + "<a href=\"javascript:alert(1)\">Bad link</a>"
                        + "<span style=\"font-size: expression(alert(1)); background: url(javascript:1)\">Text</span>"
        );

        assertFalse(result.contains("<script"));
        assertFalse(result.contains("onerror"));
        assertFalse(result.contains("javascript:"));
        assertFalse(result.contains("expression"));
        assertFalse(result.contains("background"));
    }
}
