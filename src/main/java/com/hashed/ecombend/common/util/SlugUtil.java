package com.hashed.ecombend.common.util;

import java.util.Locale;

/**
 * Utility for generating URL-safe slugs from display names.
 * Example: "Apple Watch Series 9" → "apple-watch-series-9"
 * Example: "Electronics & Gadgets!" → "electronics-gadgets"
 */
public final class SlugUtil {

    private SlugUtil() {}

    /**
     * Generates a URL-safe slug from an arbitrary string.
     * Strips special characters, lowercases, and replaces spaces with hyphens.
     *
     * @param input The name
     * @return url slug
     */
    public static String generate(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        return input
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Generates a unique slug by appending a numeric suffix if needed.
     * Use in service layer when the base slug already exists in the database.
     *
     * @param baseSlug The base slug (output of generate())
     * @param suffix   A number to append (e.g. 2 → "apple-watch-2")
     * @return A suffixed slug
     */
    public static String withSuffix(String baseSlug, int suffix) {
        return baseSlug + "-" + suffix;
    }
}