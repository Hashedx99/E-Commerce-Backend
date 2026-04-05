package com.hashed.ecombend.common.util;

/**
 * Utility for generating URL slugs from display names.
 * Examples:
 * "Apple Watch Series 9"    → "apple-watch-series-9"
 * "Electronics & Gadgets!"  → "electronics-gadgets"
 * "  Slim  Fit  Jeans  "    → "slim-fit-jeans"
 */
public final class SlugUtil {

    private SlugUtil() {
    }

    /**
     * Generates a URL slug from an arbitrary display name.
     *
     * @param input The name to convert
     * @return A lowercase hyphenated slug
     */
    public static String generate(String input) {
        if (input == null || input.isBlank()) return "";
        return input
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
//                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Appends a numeric suffix for uniqueness when the base slug already exists.
     * Example: generate("apple watch") + suffix 2 → "apple-watch-2"
     *
     * @param baseSlug
     * @param suffix
     * @return Suffixed slug
     */
    public static String withSuffix(String baseSlug, int suffix) {
        return baseSlug + "-" + suffix;
    }
}
