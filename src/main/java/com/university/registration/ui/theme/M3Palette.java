package com.university.registration.ui.theme;

/**
 * Material Design 3 (M3) Color Palettes for Light and Dark modes.
 * Formulated according to the Material 3 Dynamic Color specifications.
 */
public class M3Palette {

    public enum SeedColor {
        UNIVERSITY_BLUE("University Blue", "#1d4ed8", "#3b82f6"),
        INDIGO("Royal Indigo", "#4338ca", "#6366f1"),
        EMERALD("Emerald Green", "#047857", "#10b981"),
        CRIMSON("Crimson Red", "#b91c1c", "#ef4444");

        private final String displayName;
        private final String lightPrimary;
        private final String darkPrimary;

        SeedColor(String displayName, String lightPrimary, String darkPrimary) {
            this.displayName = displayName;
            this.lightPrimary = lightPrimary;
            this.darkPrimary = darkPrimary;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getLightPrimary() {
            return lightPrimary;
        }

        public String getDarkPrimary() {
            return darkPrimary;
        }
    }

    /**
     * Generates Material Design 3 CSS variables (-md-sys-color-*) for the given seed and mode.
     *
     * @param seed the base seed color
     * @param dark true for M3 Dark scheme, false for M3 Light scheme
     * @return CSS string with -md-sys-color-* definitions for the .root selector
     */
    public static String generateCss(SeedColor seed, boolean dark) {
        if (dark) {
            return generateDarkCss(seed);
        } else {
            return generateLightCss(seed);
        }
    }

    private static String generateDarkCss(SeedColor seed) {
        String primary = seed.getDarkPrimary();
        String primaryContainer = switch (seed) {
            case UNIVERSITY_BLUE -> "#1e3a8a";
            case INDIGO -> "#312e81";
            case EMERALD -> "#064e3b";
            case CRIMSON -> "#7f1d1d";
        };
        String onPrimaryContainer = switch (seed) {
            case UNIVERSITY_BLUE -> "#dbeafe";
            case INDIGO -> "#e0e7ff";
            case EMERALD -> "#d1fae5";
            case CRIMSON -> "#fee2e2";
        };

        return String.join("\n",
                ".root {",
                "  -md-sys-color-primary: " + primary + ";",
                "  -md-sys-color-on-primary: #001e40;",
                "  -md-sys-color-primary-container: " + primaryContainer + ";",
                "  -md-sys-color-on-primary-container: " + onPrimaryContainer + ";",
                "  -md-sys-color-secondary: #a3c9e2;",
                "  -md-sys-color-on-secondary: #003351;",
                "  -md-sys-color-secondary-container: #234863;",
                "  -md-sys-color-on-secondary-container: #cfe5ff;",
                "  -md-sys-color-tertiary: #c0c1e8;",
                "  -md-sys-color-on-tertiary: #2a2c4e;",
                "  -md-sys-color-tertiary-container: #404266;",
                "  -md-sys-color-on-tertiary-container: #dee0ff;",
                "  -md-sys-color-error: #ffb4ab;",
                "  -md-sys-color-on-error: #690005;",
                "  -md-sys-color-error-container: #93000a;",
                "  -md-sys-color-on-error-container: #ffdad6;",
                "  -md-sys-color-background: #0f141c;",
                "  -md-sys-color-on-background: #e0e2ec;",
                "  -md-sys-color-surface: #0f141c;",
                "  -md-sys-color-on-surface: #e0e2ec;",
                "  -md-sys-color-surface-variant: #43474e;",
                "  -md-sys-color-on-surface-variant: #c3c7cf;",
                "  -md-sys-color-surface-container-lowest: #0a0f16;",
                "  -md-sys-color-surface-container-low: #171c24;",
                "  -md-sys-color-surface-container: #1b2029;",
                "  -md-sys-color-surface-container-high: #262a33;",
                "  -md-sys-color-surface-container-highest: #30353e;",
                "  -md-sys-color-outline: #8d9199;",
                "  -md-sys-color-outline-variant: #43474e;",
                "  -md-sys-color-shadow: #000000;",
                "  -md-sys-color-surface-tint: " + primary + ";",
                "  -md-sys-color-inverse-surface: #e0e2ec;",
                "  -md-sys-color-inverse-on-surface: #2d3138;",
                "  -md-sys-color-inverse-primary: #0060a8;",
                "}"
        );
    }

    private static String generateLightCss(SeedColor seed) {
        String primary = seed.getLightPrimary();
        String primaryContainer = switch (seed) {
            case UNIVERSITY_BLUE -> "#dbeafe";
            case INDIGO -> "#e0e7ff";
            case EMERALD -> "#d1fae5";
            case CRIMSON -> "#fee2e2";
        };
        String onPrimaryContainer = switch (seed) {
            case UNIVERSITY_BLUE -> "#1e3a8a";
            case INDIGO -> "#312e81";
            case EMERALD -> "#064e3b";
            case CRIMSON -> "#7f1d1d";
        };

        return String.join("\n",
                ".root {",
                "  -md-sys-color-primary: " + primary + ";",
                "  -md-sys-color-on-primary: #ffffff;",
                "  -md-sys-color-primary-container: " + primaryContainer + ";",
                "  -md-sys-color-on-primary-container: " + onPrimaryContainer + ";",
                "  -md-sys-color-secondary: #50606e;",
                "  -md-sys-color-on-secondary: #ffffff;",
                "  -md-sys-color-secondary-container: #d3e5f5;",
                "  -md-sys-color-on-secondary-container: #0c1d29;",
                "  -md-sys-color-tertiary: #65587b;",
                "  -md-sys-color-on-tertiary: #ffffff;",
                "  -md-sys-color-tertiary-container: #ecdcff;",
                "  -md-sys-color-on-tertiary-container: #211634;",
                "  -md-sys-color-error: #ba1a1a;",
                "  -md-sys-color-on-error: #ffffff;",
                "  -md-sys-color-error-container: #ffdad6;",
                "  -md-sys-color-on-error-container: #410002;",
                "  -md-sys-color-background: #fdfcff;",
                "  -md-sys-color-on-background: #1a1c1e;",
                "  -md-sys-color-surface: #fdfcff;",
                "  -md-sys-color-on-surface: #1a1c1e;",
                "  -md-sys-color-surface-variant: #dfe2eb;",
                "  -md-sys-color-on-surface-variant: #43474e;",
                "  -md-sys-color-surface-container-lowest: #ffffff;",
                "  -md-sys-color-surface-container-low: #f7f9fe;",
                "  -md-sys-color-surface-container: #f1f3f9;",
                "  -md-sys-color-surface-container-high: #eaeef5;",
                "  -md-sys-color-surface-container-highest: #e4e8ef;",
                "  -md-sys-color-outline: #73777f;",
                "  -md-sys-color-outline-variant: #c3c7cf;",
                "  -md-sys-color-shadow: #000000;",
                "  -md-sys-color-surface-tint: " + primary + ";",
                "  -md-sys-color-inverse-surface: #2f3033;",
                "  -md-sys-color-inverse-on-surface: #f1f0f4;",
                "  -md-sys-color-inverse-primary: #a0c9ff;",
                "}"
        );
    }
}
