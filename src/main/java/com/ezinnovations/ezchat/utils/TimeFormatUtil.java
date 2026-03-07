package com.ezinnovations.ezchat.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class TimeFormatUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private TimeFormatUtil() {
    }

    public static String formatTimestamp(final long timestamp) {
        return FORMATTER.format(Instant.ofEpochMilli(timestamp));
    }
}
