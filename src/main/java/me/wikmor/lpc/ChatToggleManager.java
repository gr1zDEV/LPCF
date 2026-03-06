package me.wikmor.lpc;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatToggleManager {

    private final LPC plugin;
    private final File togglesFile;
    private final Set<UUID> hiddenChatPlayers = ConcurrentHashMap.newKeySet();

    public ChatToggleManager(final LPC plugin) {
        this.plugin = plugin;
        this.togglesFile = new File(plugin.getDataFolder(), "toggles.yml");
    }

    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Failed to create plugin data folder for toggles.yml.");
        }

        if (!togglesFile.exists()) {
            try {
                if (!togglesFile.createNewFile()) {
                    plugin.getLogger().warning("Failed to create toggles.yml file.");
                }
            } catch (final IOException exception) {
                plugin.getLogger().warning("Failed to create toggles.yml: " + exception.getMessage());
                return;
            }
        }

        final FileConfiguration togglesConfig = YamlConfiguration.loadConfiguration(togglesFile);
        hiddenChatPlayers.clear();

        for (final String key : togglesConfig.getKeys(false)) {
            if (!togglesConfig.getBoolean(key, false)) {
                continue;
            }
            try {
                hiddenChatPlayers.add(UUID.fromString(key));
            } catch (final IllegalArgumentException ignored) {
                plugin.getLogger().warning("Ignoring invalid UUID in toggles.yml: " + key);
            }
        }
    }

    public boolean isChatHidden(final UUID uuid) {
        return hiddenChatPlayers.contains(uuid);
    }

    public boolean toggleChat(final UUID uuid) {
        final boolean nowHidden;
        if (hiddenChatPlayers.contains(uuid)) {
            hiddenChatPlayers.remove(uuid);
            nowHidden = false;
        } else {
            hiddenChatPlayers.add(uuid);
            nowHidden = true;
        }
        save();
        return nowHidden;
    }

    public void save() {
        final FileConfiguration togglesConfig = new YamlConfiguration();
        for (final UUID uuid : hiddenChatPlayers) {
            togglesConfig.set(uuid.toString(), true);
        }

        try {
            togglesConfig.save(togglesFile);
        } catch (final IOException exception) {
            plugin.getLogger().warning("Failed to save toggles.yml: " + exception.getMessage());
        }
    }
}
