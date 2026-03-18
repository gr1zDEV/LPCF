package com.ezinnovations.ezchat.config;

import com.ezinnovations.ezchat.EzChat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class BlockedWordsConfig {

    private final EzChat plugin;

    private File file;
    private FileConfiguration config;

    public BlockedWordsConfig(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "blocked-words.yml");
        if (!file.exists()) {
            plugin.saveResource("blocked-words.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
        if (!config.isList("blocked-words")) {
            final ConfigurationSection defaults = config.getDefaults();
            if (defaults == null || !defaults.isList("blocked-words")) {
                plugin.getLogger().warning("[EzChat] blocked-words.yml is missing a valid blocked-words list. Using an empty blocked word list until it is fixed.");
            }
        }
    }

    public List<String> getBlockedWords() {
        return filterEmpty(config.getStringList("blocked-words"));
    }

    private List<String> filterEmpty(final List<String> rawValues) {
        final List<String> clean = new ArrayList<>();
        for (final String value : rawValues) {
            if (value != null && !value.trim().isEmpty()) {
                clean.add(value.trim());
            }
        }
        return clean;
    }
}
