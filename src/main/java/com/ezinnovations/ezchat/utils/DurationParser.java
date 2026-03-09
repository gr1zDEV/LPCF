package com.ezinnovations.ezchat.utils;

import java.util.Locale;
import java.util.OptionalLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationParser {

    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)([smhd])$", Pattern.CASE_INSENSITIVE);

    private DurationParser() {
    }

    public static OptionalLong parseToMillis(final String input) {
        if (input == null) {
            return OptionalLong.empty();
        }

        final Matcher matcher = DURATION_PATTERN.matcher(input.trim());
        if (!matcher.matches()) {
            return OptionalLong.empty();
        }

        final long value = Long.parseLong(matcher.group(1));
        if (value <= 0) {
            return OptionalLong.empty();
        }

        final char unit = matcher.group(2).toLowerCase(Locale.ROOT).charAt(0);
        final long multiplier = switch (unit) {
            case 's' -> 1_000L;
            case 'm' -> 60_000L;
            case 'h' -> 3_600_000L;
            case 'd' -> 86_400_000L;
            default -> -1L;
        };

        if (multiplier < 0) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(value * multiplier);
    }

    public static String formatDuration(final long millis) {
        long totalSeconds = Math.max(0, millis / 1000L);
        final long days = totalSeconds / 86400;
        totalSeconds %= 86400;
        final long hours = totalSeconds / 3600;
        totalSeconds %= 3600;
        final long minutes = totalSeconds / 60;
        final long seconds = totalSeconds % 60;

        final StringBuilder builder = new StringBuilder();
        appendUnit(builder, days, "d");
        appendUnit(builder, hours, "h");
        appendUnit(builder, minutes, "m");
        if (builder.length() == 0 || seconds > 0) {
            appendUnit(builder, seconds, "s");
        }

        return builder.toString().trim();
    }

    private static void appendUnit(final StringBuilder builder, final long value, final String unit) {
        if (value <= 0) {
            return;
        }
        builder.append(value).append(unit).append(' ');
    }
}
