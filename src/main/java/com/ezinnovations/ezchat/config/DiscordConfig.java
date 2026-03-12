package com.ezinnovations.ezchat.config;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.discord.DiscordEventType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class DiscordConfig {

    private final EzChat plugin;

    private File file;
    private FileConfiguration config;

    public DiscordConfig(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "discord.yml");
        if (!file.exists()) {
            plugin.saveResource("discord.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isFeatureEnabled() {
        return config.getBoolean("features.enabled", false);
    }

    public boolean useEmbeds() {
        return config.getBoolean("webhook.use-embeds", true);
    }

    public String getDefaultWebhook() {
        return trim(config.getString("webhooks.default", ""));
    }

    public String getWebhookFor(final DiscordEventType type) {
        final String path = switch (type) {
            case PUBLIC_CHAT -> "webhooks.public-chat";
            case PRIVATE_MESSAGES -> "webhooks.private-messages";
            case MAIL -> "webhooks.mail";
            case MUTE_ACTIONS -> "webhooks.mute-actions";
            case AUDIT_ACTIONS -> "webhooks.audit-actions";
            case SERVER_BROADCASTS -> "webhooks.server-broadcasts";
        };
        return trim(config.getString(path, ""));
    }

    public boolean isEventEnabled(final DiscordEventType type) {
        final String path = switch (type) {
            case PUBLIC_CHAT -> "events.public-chat";
            case PRIVATE_MESSAGES -> "events.private-messages";
            case MAIL -> "events.mail";
            case MUTE_ACTIONS -> "events.mute-actions";
            case AUDIT_ACTIONS -> "events.audit-actions";
            case SERVER_BROADCASTS -> "events.server-broadcasts";
        };
        return config.getBoolean(path, false);
    }

    public String getUsernameMode() {
        return config.getString("identity.username-mode", "minecraft-name").toLowerCase();
    }

    public String getStaticUsername() {
        return config.getString("identity.static-username", "EzChat");
    }

    public String getAvatarMode() {
        return config.getString("identity.avatar-mode", "minecraft-head").toLowerCase();
    }

    public String getStaticAvatarUrl() {
        return trim(config.getString("identity.static-avatar-url", ""));
    }

    public String getAvatarProvider() {
        return config.getString("avatar.provider", "crafatar").toLowerCase();
    }

    public String getCustomAvatarUrl() {
        return trim(config.getString("avatar.custom-url", ""));
    }

    public String getFormat(final String key, final String fallback) {
        return config.getString("format." + key, fallback);
    }

    public String getMessage(final String key, final String fallback) {
        return config.getString("messages." + key, fallback);
    }

    public Map<String, String> fillPlaceholders(final String template, final Map<String, String> values) {
        final Map<String, String> output = new HashMap<>();
        for (final Map.Entry<String, String> entry : values.entrySet()) {
            output.put(entry.getKey(), entry.getValue() == null ? "unknown" : entry.getValue());
        }
        return output;
    }

    private String trim(final String input) {
        return input == null ? "" : input.trim();
    }
}
