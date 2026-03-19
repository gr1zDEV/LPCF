package com.ezinnovations.ezchat.config;

import com.ezinnovations.ezchat.EzChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Locale;

public final class PlaceholdersConfig {

    private static final String DEFAULT_RAW_MODE = "true-false";

    private final EzChat plugin;
    private File file;
    private FileConfiguration config;

    public PlaceholdersConfig(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "placeholders.yml");
        if (!file.exists()) {
            plugin.saveResource("placeholders.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isFeatureEnabled() {
        return config.getBoolean("features.enabled", true);
    }

    public boolean areSettingsPlaceholdersEnabled() {
        return config.getBoolean("features.settings-placeholders.enabled", true);
    }

    public String getRawMode() {
        final String configuredMode = config.getString("settings.raw-mode", DEFAULT_RAW_MODE);
        if (configuredMode == null) {
            return DEFAULT_RAW_MODE;
        }

        final String normalized = configuredMode.toLowerCase(Locale.ROOT);
        if ("on-off".equals(normalized)) {
            return "on-off";
        }
        return DEFAULT_RAW_MODE;
    }

    public String getFormattedTrueText() {
        return config.getString("formatting.true-text", "&aON");
    }

    public String getFormattedFalseText() {
        return config.getString("formatting.false-text", "&cOFF");
    }
}
