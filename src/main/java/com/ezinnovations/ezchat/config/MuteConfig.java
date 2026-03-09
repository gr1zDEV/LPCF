package com.ezinnovations.ezchat.config;

import com.ezinnovations.ezchat.EzChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class MuteConfig {

    private final EzChat plugin;
    private File file;
    private FileConfiguration config;

    public MuteConfig(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "mute.yml");
        if (!file.exists()) {
            plugin.saveResource("mute.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isFeatureEnabled() {
        return config.getBoolean("features.enabled", true);
    }

    public boolean blockPublicChat() {
        return config.getBoolean("settings.block-public-chat", true);
    }

    public boolean blockPrivateMessages() {
        return config.getBoolean("settings.block-private-messages", true);
    }

    public boolean blockMail() {
        return config.getBoolean("settings.block-mail", true);
    }

    public String getMessage(final String path, final String fallback) {
        return config.getString("messages." + path, fallback);
    }
}
