package com.ezinnovations.ezchat.moderation;

public record AdvertisingDetectionResult(boolean detected, AdvertisingType type, String match) {

    public static AdvertisingDetectionResult clear() {
        return new AdvertisingDetectionResult(false, null, "");
    }

    public static AdvertisingDetectionResult flagged(final AdvertisingType type, final String match) {
        return new AdvertisingDetectionResult(true, type, match);
    }
}
