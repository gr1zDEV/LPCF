package com.ezinnovations.ezchat.moderation;

public record ProfanityDetectionResult(boolean detected, ProfanityMatchType type, String match) {

    public static ProfanityDetectionResult clear() {
        return new ProfanityDetectionResult(false, null, "");
    }

    public static ProfanityDetectionResult flagged(final ProfanityMatchType type, final String match) {
        return new ProfanityDetectionResult(true, type, match);
    }
}
