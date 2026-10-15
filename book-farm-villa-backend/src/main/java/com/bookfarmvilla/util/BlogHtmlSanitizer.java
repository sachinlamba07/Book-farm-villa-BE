package com.bookfarmvilla.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class BlogHtmlSanitizer {

    private static final Pattern FONT_SIZE = Pattern.compile(
            "\\d+(?:\\.\\d+)?(?:px|rem|em|%)", Pattern.CASE_INSENSITIVE
    );
    private static final Pattern COLOR = Pattern.compile(
            "(?:#[\\da-f]{3,8}|[a-z]{1,24}|(?:rgb|hsl)a?\\([\\d.% ,/+-]+\\))",
            Pattern.CASE_INSENSITIVE
    );
    private static final Safelist ALLOWED_HTML = new Safelist()
            .addTags("a", "blockquote", "br", "em", "h1", "h2", "h3", "h4", "img", "li",
                    "ol", "p", "span", "strong", "u", "ul")
            .addAttributes("a", "href", "target", "rel")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addAttributes("img", "src", "alt")
            .addProtocols("img", "src", "http", "https")
            .addAttributes("span", "style");

    private BlogHtmlSanitizer() {
    }

    public static String sanitize(String html) {
        var body = Jsoup.parseBodyFragment(html).body();
        for (Element styled : body.select("[style]")) {
            if (!styled.normalName().equals("span")) {
                styled.removeAttr("style");
                continue;
            }
            String safeStyle = Arrays.stream(styled.attr("style").split(";"))
                    .map(declaration -> declaration.split(":", 2))
                    .filter(parts -> parts.length == 2 && isSafeStyle(parts[0].trim(), parts[1].trim()))
                    .map(parts -> parts[0].trim().toLowerCase(Locale.ROOT) + ": " + parts[1].trim())
                    .collect(Collectors.joining("; "));
            if (safeStyle.isBlank()) {
                styled.removeAttr("style");
            } else {
                styled.attr("style", safeStyle);
            }
        }

        String safeBody = Jsoup.clean(
                body.html(),
                "",
                ALLOWED_HTML,
                new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false)
        );
        var cleaned = Jsoup.parseBodyFragment(safeBody).body();
        for (Element link : cleaned.select("a")) {
            link.attr("target", "_blank");
            link.attr("rel", "noopener noreferrer");
        }
        return cleaned.html();
    }

    private static boolean isSafeStyle(String property, String value) {
        return switch (property.toLowerCase(Locale.ROOT)) {
            case "font-size" -> FONT_SIZE.matcher(value).matches();
            case "color" -> COLOR.matcher(value).matches();
            default -> false;
        };
    }
}
