package com.ezinnovations.ezchat.config;

import com.ezinnovations.ezchat.EzChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class LogsConfig {

    private final EzChat plugin;

    private File file;
    private FileConfiguration config;
    private DateTimeFormatter timeFormatter;

    public LogsConfig(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "logs.yml");
        if (!file.exists()) {
            plugin.saveResource("logs.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
        final String pattern = config.getString("settings.time-format", "yyyy-MM-dd HH:mm:ss");
        timeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.ROOT).withZone(ZoneId.systemDefault());
    }

    public boolean isFeatureEnabled() {
        return config.getBoolean("features.enabled", true);
    }

    public boolean isLoggingEnabled() {
        return config.getBoolean("settings.logging-enabled", true);
    }

    public boolean isPublicChatLoggingEnabled() {
        return config.getBoolean("settings.public-chat", true);
    }

    public boolean isPrivateMessageLoggingEnabled() {
        return config.getBoolean("settings.private-messages", true);
    }

    public boolean isMailLoggingEnabled() {
        return config.getBoolean("settings.mail", true);
    }

    public boolean isAuditLoggingEnabled() {
        return config.getBoolean("settings.audit", true);
    }

    public int getPageSize() {
        return Math.max(1, config.getInt("settings.page-size", 10));
    }

    public String getMessage(final String path, final String fallback) {
        return config.getString("messages." + path, fallback);
    }

    public String formatTimestamp(final long timestamp) {
        return timeFormatter.format(Instant.ofEpochMilli(timestamp));
    }
}
