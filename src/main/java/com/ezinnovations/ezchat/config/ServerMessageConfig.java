package com.ezinnovations.ezchat.config;

import com.ezinnovations.ezchat.EzChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class ServerMessageConfig {

    private final EzChat plugin;

    private File file;
    private FileConfiguration config;

    public ServerMessageConfig(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "server-message.yml");
        if (!file.exists()) {
            plugin.saveResource("server-message.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isFeatureEnabled() {
        return config.getBoolean("features.enabled", true);
    }

    public boolean shouldLogBroadcasts() {
        return config.getBoolean("settings.log-broadcasts", true);
    }

    public boolean shouldLogToggleActions() {
        return config.getBoolean("settings.log-toggle-actions", true);
    }

    public String getFormat(final String key, final String fallback) {
        return config.getString("formats." + key, fallback);
    }

    public String getMessage(final String key, final String fallback) {
        return config.getString("messages." + key, fallback);
    }
}
