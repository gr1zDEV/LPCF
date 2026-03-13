package com.ezinnovations.ezchat.config;

import com.ezinnovations.ezchat.EzChat;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class JoinLeaveConfig {

    private final EzChat plugin;

    private File file;
    private FileConfiguration config;

    public JoinLeaveConfig(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "join-leave.yml");
        if (!file.exists()) {
            plugin.saveResource("join-leave.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isFeatureEnabled() {
        return config.getBoolean("features.enabled", true);
    }

    public boolean shouldLogJoinLeaveMessages() {
        return config.getBoolean("settings.log-join-leave-messages", true);
    }

    public boolean isDefaultEnabled() {
        return config.getBoolean("settings.default-enabled", true);
    }

    public boolean useVanillaJoinMessage() {
        return config.getBoolean("settings.use-vanilla-join-message", true);
    }

    public boolean useVanillaLeaveMessage() {
        return config.getBoolean("settings.use-vanilla-leave-message", true);
    }

    public String getFormat(final String key, final String fallback) {
        return config.getString("formats." + key, fallback);
    }

    public String getMessage(final String key, final String fallback) {
        return config.getString("messages." + key, fallback);
    }

    public boolean isSoundEnabled(final String type) {
        return config.getBoolean("sounds." + type + ".enabled", false);
    }

    public Sound getSound(final String type, final String fallback) {
        final String raw = config.getString("sounds." + type + ".sound", fallback);
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return Sound.valueOf(raw.toUpperCase());
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }

    public float getSoundVolume(final String type) {
        return (float) config.getDouble("sounds." + type + ".volume", 1.0D);
    }

    public float getSoundPitch(final String type) {
        return (float) config.getDouble("sounds." + type + ".pitch", 1.0D);
    }
}
