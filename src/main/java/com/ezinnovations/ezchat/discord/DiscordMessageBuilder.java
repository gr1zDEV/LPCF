package com.ezinnovations.ezchat.discord;

import com.ezinnovations.ezchat.config.DiscordConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DiscordMessageBuilder {

    private final DiscordConfig config;
    private final AvatarUrlResolver avatarUrlResolver;

    public DiscordMessageBuilder(final DiscordConfig config, final AvatarUrlResolver avatarUrlResolver) {
        this.config = config;
        this.avatarUrlResolver = avatarUrlResolver;
    }

    public String buildPayload(final DiscordEventType type,
                               final UUID actorUuid,
                               final String actorName,
                               final String formatKey,
                               final String fallbackFormat,
                               final Map<String, String> placeholders) {
        final String username = resolveUsername(actorName);
        final String avatarUrl = avatarUrlResolver.resolveAvatarUrl(actorUuid, actorName);
        final String content = applyPlaceholders(config.getFormat(formatKey, fallbackFormat), placeholders);

        final List<String> fields = new ArrayList<>();
        fields.add(jsonField("username", username));
        if (!avatarUrl.isBlank()) {
            fields.add(jsonField("avatar_url", avatarUrl));
        }

        if (!config.useEmbeds()) {
            fields.add(jsonField("content", content));
            return "{" + String.join(",", fields) + "}";
        }

        final String title = switch (type) {
            case PUBLIC_CHAT -> "Public Chat";
            case PRIVATE_MESSAGES -> "Private Message";
            case MAIL -> "Mail";
            case MUTE_ACTIONS -> "Mute Action";
            case AUDIT_ACTIONS -> "Audit Action";
            case SERVER_BROADCASTS -> "Server Broadcast";
            case DEATH_MESSAGES -> "Death Message";
            default -> "Notification";
        };

        fields.add("\"embeds\":[{" + jsonField("title", title) + "," + jsonField("description", content) + "}]");
        return "{" + String.join(",", fields) + "}";
    }

    private String resolveUsername(final String actorName) {
        if ("static".equalsIgnoreCase(config.getUsernameMode())) {
            return config.getStaticUsername();
        }
        if (actorName == null || actorName.isBlank()) {
            return config.getStaticUsername();
        }
        return actorName;
    }

    private String applyPlaceholders(final String template, final Map<String, String> placeholders) {
        String formatted = template;
        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            formatted = formatted.replace("{" + entry.getKey() + "}", entry.getValue() == null ? "unknown" : entry.getValue());
        }
        return formatted;
    }

    private String jsonField(final String key, final String value) {
        return "\"" + escapeJson(key) + "\":\"" + escapeJson(value) + "\"";
    }

    private String escapeJson(final String input) {
        final String value = input == null ? "" : input;
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
