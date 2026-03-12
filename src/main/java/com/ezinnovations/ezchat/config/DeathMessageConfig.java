package com.ezinnovations.ezchat.config;

import com.ezinnovations.ezchat.EzChat;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class DeathMessageConfig {

    private final EzChat plugin;

    private File file;
    private FileConfiguration config;

    public DeathMessageConfig(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "death-message.yml");
        if (!file.exists()) {
            plugin.saveResource("death-message.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isFeatureEnabled() {
        return config.getBoolean("features.enabled", true);
    }

    public boolean shouldLogDeathMessages() {
        return config.getBoolean("settings.log-death-messages", true);
    }

    public boolean useVanillaDeathMessage() {
        return config.getBoolean("settings.use-vanilla-death-message", true);
    }

    public boolean isDefaultEnabled() {
        return config.getBoolean("settings.default-enabled", true);
    }

    public String getFormat(final String key, final String fallback) {
        return config.getString("formats." + key, fallback);
    }

    public boolean isSoundEnabled() {
        return config.getBoolean("sound.enabled", true);
    }

    public Sound getSound() {
        final String raw = config.getString("sound.sound", "ENTITY_BLAZE_DEATH");
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return Sound.valueOf(raw.toUpperCase());
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }

    public float getSoundVolume() {
        return (float) config.getDouble("sound.volume", 1.0D);
    }

    public float getSoundPitch() {
        return (float) config.getDouble("sound.pitch", 1.0D);
    }

    public String getMessage(final String key, final String fallback) {
        return config.getString("messages." + key, fallback);
    }
}
