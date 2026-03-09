package com.ezinnovations.ezchat.database;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.database.repository.IgnoreRepository;
import com.ezinnovations.ezchat.database.repository.MailRepository;
import com.ezinnovations.ezchat.database.repository.ToggleRepository;
import com.ezinnovations.ezchat.managers.IgnoreManager;
import com.ezinnovations.ezchat.model.MailEntry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LegacyYamlMigration {

    private final EzChat plugin;
    private final ToggleRepository toggleRepository;
    private final IgnoreRepository ignoreRepository;
    private final MailRepository mailRepository;

    public LegacyYamlMigration(final EzChat plugin,
                               final ToggleRepository toggleRepository,
                               final IgnoreRepository ignoreRepository,
                               final MailRepository mailRepository) {
        this.plugin = plugin;
        this.toggleRepository = toggleRepository;
        this.ignoreRepository = ignoreRepository;
        this.mailRepository = mailRepository;
    }

    public void migrateIfNeeded() {
        migrateToggles();
        migrateIgnores();
        migrateMail();
    }

    private void migrateToggles() {
        final File togglesFile = new File(plugin.getDataFolder(), "toggles.yml");
        if (!togglesFile.exists()) {
            return;
        }

        try {
            if (!toggleRepository.isEmpty()) {
                return;
            }

            final FileConfiguration config = YamlConfiguration.loadConfiguration(togglesFile);
            final Map<UUID, ToggleImportState> byPlayer = new HashMap<>();
            importToggleSection(config.getConfigurationSection("chat-hidden"), byPlayer, ToggleSource.CHAT);
            importToggleSection(config.getConfigurationSection("private-messages-disabled"), byPlayer, ToggleSource.MSG);
            importToggleSection(config.getConfigurationSection("mail-disabled"), byPlayer, ToggleSource.MAIL);

            for (final String key : config.getKeys(false)) {
                if ("chat-hidden".equals(key) || "private-messages-disabled".equals(key) || "mail-disabled".equals(key)) {
                    continue;
                }
                if (!config.getBoolean(key, false)) {
                    continue;
                }
                try {
                    byPlayer.computeIfAbsent(UUID.fromString(key), ignored -> new ToggleImportState()).chatEnabled = false;
                } catch (final IllegalArgumentException ignored) {
                    // ignore legacy non-uuid key
                }
            }

            for (final Map.Entry<UUID, ToggleImportState> entry : byPlayer.entrySet()) {
                final UUID playerUuid = entry.getKey();
                final ToggleImportState state = entry.getValue();
                toggleRepository.upsert(playerUuid, state.chatEnabled, state.msgEnabled, state.mailEnabled);
            }

            backupLegacyFile(togglesFile);
            plugin.getLogger().info("Migrated toggles.yml into SQLite.");
        } catch (final SQLException exception) {
            plugin.getLogger().severe("Failed to migrate toggles.yml: " + exception.getMessage());
        }
    }

    private void migrateIgnores() {
        final File ignoresFile = new File(plugin.getDataFolder(), "ignores.yml");
        if (!ignoresFile.exists()) {
            return;
        }

        try {
            if (!ignoreRepository.isEmpty()) {
                return;
            }

            final FileConfiguration config = YamlConfiguration.loadConfiguration(ignoresFile);
            final ConfigurationSection playersSection = config.getConfigurationSection("players");
            if (playersSection == null) {
                return;
            }

            for (final String ownerRaw : playersSection.getKeys(false)) {
                final UUID ownerUuid;
                try {
                    ownerUuid = UUID.fromString(ownerRaw);
                } catch (final IllegalArgumentException ignored) {
                    continue;
                }

                final ConfigurationSection ignoredSection = playersSection.getConfigurationSection(ownerRaw + ".ignored");
                if (ignoredSection == null) {
                    continue;
                }

                for (final String targetRaw : ignoredSection.getKeys(false)) {
                    try {
                        final UUID targetUuid = UUID.fromString(targetRaw);
                        final String typeRaw = ignoredSection.getString(targetRaw, "ALL");
                        final IgnoreManager.IgnoreType type = IgnoreManager.IgnoreType.valueOf(typeRaw.toUpperCase());
                        ignoreRepository.setIgnore(ownerUuid, targetUuid, type);
                    } catch (final IllegalArgumentException ignored) {
                        // bad uuid or type
                    }
                }
            }

            backupLegacyFile(ignoresFile);
            plugin.getLogger().info("Migrated ignores.yml into SQLite.");
        } catch (final SQLException exception) {
            plugin.getLogger().severe("Failed to migrate ignores.yml: " + exception.getMessage());
        }
    }

    private void migrateMail() {
        final File mailFile = new File(plugin.getDataFolder(), "mail.yml");
        if (!mailFile.exists()) {
            return;
        }

        try {
            if (!mailRepository.isEmpty()) {
                return;
            }

            final FileConfiguration config = YamlConfiguration.loadConfiguration(mailFile);
            final ConfigurationSection playersSection = config.getConfigurationSection("players");
            if (playersSection == null) {
                return;
            }

            for (final String ownerRaw : playersSection.getKeys(false)) {
                final UUID ownerUuid;
                try {
                    ownerUuid = UUID.fromString(ownerRaw);
                } catch (final IllegalArgumentException ignored) {
                    continue;
                }

                final String ownerName = plugin.getServer().getOfflinePlayer(ownerUuid).getName();
                final String basePath = "players." + ownerRaw;

                final List<Map<?, ?>> sent = config.getMapList(basePath + ".sent");
                for (final Map<?, ?> map : sent) {
                    try {
                        final MailEntry entry = new MailEntry(
                                String.valueOf(map.get("id")),
                                ownerUuid,
                                ownerName,
                                UUID.fromString(String.valueOf(map.get("receiver"))),
                                String.valueOf(map.get("receiver-name")),
                                String.valueOf(map.get("message")),
                                asLong(map.get("timestamp")),
                                true
                        );
                        mailRepository.upsert(entry);
                    } catch (final Exception ignored) {
                        // invalid entry
                    }
                }

                final List<Map<?, ?>> inbox = config.getMapList(basePath + ".inbox");
                for (final Map<?, ?> map : inbox) {
                    try {
                        final MailEntry entry = new MailEntry(
                                String.valueOf(map.get("id")),
                                UUID.fromString(String.valueOf(map.get("sender"))),
                                String.valueOf(map.get("sender-name")),
                                ownerUuid,
                                ownerName,
                                String.valueOf(map.get("message")),
                                asLong(map.get("timestamp")),
                                asBoolean(map.get("read"))
                        );
                        mailRepository.upsert(entry);
                    } catch (final Exception ignored) {
                        // invalid entry
                    }
                }
            }

            backupLegacyFile(mailFile);
            plugin.getLogger().info("Migrated mail.yml into SQLite.");
        } catch (final SQLException exception) {
            plugin.getLogger().severe("Failed to migrate mail.yml: " + exception.getMessage());
        }
    }

    private void importToggleSection(final ConfigurationSection section,
                                     final Map<UUID, ToggleImportState> byPlayer,
                                     final ToggleSource source) {
        if (section == null) {
            return;
        }

        for (final String uuidRaw : section.getKeys(false)) {
            if (!section.getBoolean(uuidRaw, false)) {
                continue;
            }
            try {
                final ToggleImportState state = byPlayer.computeIfAbsent(UUID.fromString(uuidRaw), ignored -> new ToggleImportState());
                switch (source) {
                    case CHAT -> state.chatEnabled = false;
                    case MSG -> state.msgEnabled = false;
                    case MAIL -> state.mailEnabled = false;
                }
            } catch (final IllegalArgumentException ignored) {
                // bad uuid
            }
        }
    }

    private void backupLegacyFile(final File file) {
        final File backup = new File(file.getParentFile(), file.getName() + ".bak");
        if (backup.exists()) {
            return;
        }

        try {
            Files.move(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException exception) {
            plugin.getLogger().warning("Failed to backup legacy file " + file.getName() + ": " + exception.getMessage());
        }
    }

    private long asLong(final Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private boolean asBoolean(final Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static final class ToggleImportState {
        private boolean chatEnabled = true;
        private boolean msgEnabled = true;
        private boolean mailEnabled = true;
    }

    private enum ToggleSource {
        CHAT,
        MSG,
        MAIL
    }
}
