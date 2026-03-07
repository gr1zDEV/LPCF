package com.ezinnovations.ezchat.managers;

import com.ezinnovations.ezchat.EzChat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class IgnoreManager {

    public enum IgnoreType {
        ALL,
        CHAT,
        MSG,
        MAIL
    }

    private final EzChat plugin;
    private final File ignoresFile;
    private final Map<UUID, Map<UUID, IgnoreType>> ignoresByViewer = new ConcurrentHashMap<>();

    public IgnoreManager(final EzChat plugin) {
        this.plugin = plugin;
        this.ignoresFile = new File(plugin.getDataFolder(), "ignores.yml");
    }

    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Failed to create plugin data folder for ignores.yml.");
        }

        if (!ignoresFile.exists()) {
            try {
                if (!ignoresFile.createNewFile()) {
                    plugin.getLogger().warning("Failed to create ignores.yml file.");
                }
            } catch (final IOException exception) {
                plugin.getLogger().warning("Failed to create ignores.yml: " + exception.getMessage());
                return;
            }
        }

        final FileConfiguration ignoresConfig = YamlConfiguration.loadConfiguration(ignoresFile);
        ignoresByViewer.clear();

        final ConfigurationSection playersSection = ignoresConfig.getConfigurationSection("players");
        if (playersSection == null) {
            return;
        }

        for (final String viewerUuidRaw : playersSection.getKeys(false)) {
            final UUID viewerUuid;
            try {
                viewerUuid = UUID.fromString(viewerUuidRaw);
            } catch (final IllegalArgumentException ignored) {
                plugin.getLogger().warning("Ignoring invalid viewer UUID in ignores.yml: " + viewerUuidRaw);
                continue;
            }

            final ConfigurationSection ignoredSection = playersSection.getConfigurationSection(viewerUuidRaw + ".ignored");
            if (ignoredSection == null) {
                continue;
            }

            final Map<UUID, IgnoreType> viewerIgnores = new ConcurrentHashMap<>();
            for (final String ignoredUuidRaw : ignoredSection.getKeys(false)) {
                final UUID ignoredUuid;
                try {
                    ignoredUuid = UUID.fromString(ignoredUuidRaw);
                } catch (final IllegalArgumentException ignored) {
                    plugin.getLogger().warning("Ignoring invalid ignored UUID in ignores.yml for " + viewerUuidRaw + ": " + ignoredUuidRaw);
                    continue;
                }

                final String ignoreTypeRaw = ignoredSection.getString(ignoredUuidRaw);
                if (ignoreTypeRaw == null) {
                    continue;
                }

                try {
                    final IgnoreType ignoreType = IgnoreType.valueOf(ignoreTypeRaw.toUpperCase());
                    viewerIgnores.put(ignoredUuid, ignoreType);
                } catch (final IllegalArgumentException ignored) {
                    plugin.getLogger().warning("Ignoring invalid ignore type in ignores.yml for " + viewerUuidRaw + " -> " + ignoredUuidRaw + ": " + ignoreTypeRaw);
                }
            }

            if (!viewerIgnores.isEmpty()) {
                ignoresByViewer.put(viewerUuid, viewerIgnores);
            }
        }
    }

    public void save() {
        final FileConfiguration ignoresConfig = new YamlConfiguration();

        for (final Map.Entry<UUID, Map<UUID, IgnoreType>> viewerEntry : ignoresByViewer.entrySet()) {
            final UUID viewerUuid = viewerEntry.getKey();
            for (final Map.Entry<UUID, IgnoreType> ignoredEntry : viewerEntry.getValue().entrySet()) {
                ignoresConfig.set("players." + viewerUuid + ".ignored." + ignoredEntry.getKey(), ignoredEntry.getValue().name());
            }
        }

        try {
            ignoresConfig.save(ignoresFile);
        } catch (final IOException exception) {
            plugin.getLogger().warning("Failed to save ignores.yml: " + exception.getMessage());
        }
    }

    public boolean isIgnoring(final UUID viewer, final UUID sender, final IgnoreType type) {
        final Map<UUID, IgnoreType> viewerIgnores = ignoresByViewer.get(viewer);
        if (viewerIgnores == null) {
            return false;
        }

        final IgnoreType storedType = viewerIgnores.get(sender);
        if (storedType == null) {
            return false;
        }

        return storedType == IgnoreType.ALL || storedType == type;
    }

    public boolean toggleIgnore(final UUID viewer, final UUID target, final IgnoreType type) {
        final Map<UUID, IgnoreType> viewerIgnores = ignoresByViewer.computeIfAbsent(viewer, ignored -> new ConcurrentHashMap<>());
        final IgnoreType current = viewerIgnores.get(target);

        if (current == type) {
            viewerIgnores.remove(target);
            cleanupViewerIfEmpty(viewer, viewerIgnores);
            return false;
        }

        viewerIgnores.put(target, type);
        return true;
    }

    private void cleanupViewerIfEmpty(final UUID viewer, final Map<UUID, IgnoreType> viewerIgnores) {
        if (viewerIgnores.isEmpty()) {
            ignoresByViewer.remove(viewer);
        }
    }
}
