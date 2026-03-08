package com.ezinnovations.ezchat.managers;

import com.ezinnovations.ezchat.EzChat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatToggleManager {

    private final EzChat plugin;
    private final File togglesFile;
    private final Set<UUID> hiddenChatPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> privateMessagesDisabledPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> mailDisabledPlayers = ConcurrentHashMap.newKeySet();

    public ChatToggleManager(final EzChat plugin) {
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
        privateMessagesDisabledPlayers.clear();
        mailDisabledPlayers.clear();

        final ConfigurationSection chatHiddenSection = togglesConfig.getConfigurationSection("chat-hidden");
        if (chatHiddenSection != null) {
            loadEnabledUuids(chatHiddenSection, hiddenChatPlayers, "chat-hidden");
        } else {
            // Backward compatibility with pre-section format where root keys were chat toggle UUIDs.
            for (final String key : togglesConfig.getKeys(false)) {
                if (!togglesConfig.getBoolean(key, false)) {
                    continue;
                }
                try {
                    hiddenChatPlayers.add(UUID.fromString(key));
                } catch (final IllegalArgumentException ignored) {
                    // Ignore non-UUID root keys (e.g. the new sections).
                }
            }
        }

        final ConfigurationSection privateMessageSection = togglesConfig.getConfigurationSection("private-messages-disabled");
        if (privateMessageSection != null) {
            loadEnabledUuids(privateMessageSection, privateMessagesDisabledPlayers, "private-messages-disabled");
        }

        final ConfigurationSection mailSection = togglesConfig.getConfigurationSection("mail-disabled");
        if (mailSection != null) {
            loadEnabledUuids(mailSection, mailDisabledPlayers, "mail-disabled");
        }
    }

    private void loadEnabledUuids(final ConfigurationSection section, final Set<UUID> destination, final String sectionName) {
        for (final String key : section.getKeys(false)) {
            if (!section.getBoolean(key, false)) {
                continue;
            }
            try {
                destination.add(UUID.fromString(key));
            } catch (final IllegalArgumentException ignored) {
                plugin.getLogger().warning("Ignoring invalid UUID in toggles.yml section '" + sectionName + "': " + key);
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

    public boolean arePrivateMessagesDisabled(final UUID uuid) {
        return privateMessagesDisabledPlayers.contains(uuid);
    }

    public boolean setChatHidden(final UUID uuid, final boolean hidden) {
        final boolean changed;
        if (hidden) {
            changed = hiddenChatPlayers.add(uuid);
        } else {
            changed = hiddenChatPlayers.remove(uuid);
        }

        if (changed) {
            save();
        }

        return changed;
    }

    public boolean setPrivateMessagesDisabled(final UUID uuid, final boolean disabled) {
        final boolean changed;
        if (disabled) {
            changed = privateMessagesDisabledPlayers.add(uuid);
        } else {
            changed = privateMessagesDisabledPlayers.remove(uuid);
        }

        if (changed) {
            save();
        }

        return changed;
    }

    public boolean togglePrivateMessages(final UUID uuid) {
        final boolean nowDisabled;
        if (privateMessagesDisabledPlayers.contains(uuid)) {
            privateMessagesDisabledPlayers.remove(uuid);
            nowDisabled = false;
        } else {
            privateMessagesDisabledPlayers.add(uuid);
            nowDisabled = true;
        }
        save();
        return nowDisabled;
    }

    public boolean isMailDisabled(final UUID uuid) {
        return mailDisabledPlayers.contains(uuid);
    }

    public boolean setMailDisabled(final UUID uuid, final boolean disabled) {
        final boolean changed;
        if (disabled) {
            changed = mailDisabledPlayers.add(uuid);
        } else {
            changed = mailDisabledPlayers.remove(uuid);
        }

        if (changed) {
            save();
        }

        return changed;
    }

    public boolean toggleMail(final UUID uuid) {
        final boolean nowDisabled;
        if (mailDisabledPlayers.contains(uuid)) {
            mailDisabledPlayers.remove(uuid);
            nowDisabled = false;
        } else {
            mailDisabledPlayers.add(uuid);
            nowDisabled = true;
        }
        save();
        return nowDisabled;
    }

    public void save() {
        final FileConfiguration togglesConfig = new YamlConfiguration();

        for (final UUID uuid : hiddenChatPlayers) {
            togglesConfig.set("chat-hidden." + uuid, true);
        }

        for (final UUID uuid : privateMessagesDisabledPlayers) {
            togglesConfig.set("private-messages-disabled." + uuid, true);
        }

        for (final UUID uuid : mailDisabledPlayers) {
            togglesConfig.set("mail-disabled." + uuid, true);
        }

        try {
            togglesConfig.save(togglesFile);
        } catch (final IOException exception) {
            plugin.getLogger().warning("Failed to save toggles.yml: " + exception.getMessage());
        }
    }
}
