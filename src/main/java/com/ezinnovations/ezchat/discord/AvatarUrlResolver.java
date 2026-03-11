package com.ezinnovations.ezchat.discord;

import com.ezinnovations.ezchat.config.DiscordConfig;

import java.util.UUID;

public final class AvatarUrlResolver {

    private final DiscordConfig config;

    public AvatarUrlResolver(final DiscordConfig config) {
        this.config = config;
    }

    public String resolveAvatarUrl(final UUID uuid, final String username) {
        final String mode = config.getAvatarMode();
        if ("none".equalsIgnoreCase(mode)) {
            return "";
        }

        if ("static".equalsIgnoreCase(mode)) {
            return config.getStaticAvatarUrl();
        }

        final String safeUsername = username == null || username.isBlank() ? "unknown" : username;
        final String uuidNoDash = uuid == null ? "" : uuid.toString().replace("-", "");
        final String uuidString = uuid == null ? "" : uuid.toString();

        return switch (config.getAvatarProvider()) {
            case "minotar" -> "https://minotar.net/helm/" + (!safeUsername.equals("unknown") ? safeUsername : uuidNoDash) + "/64.png";
            case "custom" -> config.getCustomAvatarUrl()
                    .replace("{uuid}", uuidString)
                    .replace("{username}", safeUsername);
            case "crafatar" -> {
                if (!uuidNoDash.isBlank()) {
                    yield "https://crafatar.com/avatars/" + uuidNoDash + "?size=64&overlay";
                }
                yield "https://minotar.net/helm/" + safeUsername + "/64.png";
            }
            default -> "https://crafatar.com/avatars/" + uuidNoDash + "?size=64&overlay";
        };
    }
}
