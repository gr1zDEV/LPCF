package com.ezinnovations.ezchat.config;

import com.ezinnovations.ezchat.EzChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class AntiFloodConfig {

    private final EzChat plugin;

    private File file;
    private FileConfiguration config;

    public AntiFloodConfig(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "anti-flood.yml");
        if (!file.exists()) {
            plugin.saveResource("anti-flood.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isFeatureEnabled() {
        return config.getBoolean("features.enabled", true);
    }

    public boolean shouldCheckPublicChat() {
        return config.getBoolean("checks.public-chat", true);
    }

    public boolean shouldCheckPrivateMessages() {
        return config.getBoolean("checks.private-messages", true);
    }

    public boolean shouldCheckStaffChat() {
        return config.getBoolean("checks.staff-chat", false);
    }

    public boolean isCooldownEnabled() {
        return config.getBoolean("cooldown.enabled", true);
    }

    public long getCooldownMilliseconds() {
        return Math.max(0L, config.getLong("cooldown.milliseconds", 1500L));
    }

    public boolean isDuplicateEnabled() {
        return config.getBoolean("duplicate.enabled", true);
    }

    public long getDuplicateWindowMilliseconds() {
        return Math.max(1L, config.getLong("duplicate.window-seconds", 15L)) * 1000L;
    }

    public int getMaxDuplicates() {
        return Math.max(1, config.getInt("duplicate.max-duplicates", 2));
    }

    public boolean shouldNormalizeCase() {
        return config.getBoolean("duplicate.normalize-case", true);
    }

    public boolean shouldTrimSpaces() {
        return config.getBoolean("duplicate.trim-spaces", true);
    }

    public boolean isBurstEnabled() {
        return config.getBoolean("burst.enabled", true);
    }

    public long getBurstWindowMilliseconds() {
        return Math.max(1L, config.getLong("burst.window-seconds", 5L)) * 1000L;
    }

    public int getMaxMessages() {
        return Math.max(1, config.getInt("burst.max-messages", 4));
    }

    public boolean shouldBlockMessage() {
        return config.getBoolean("actions.block-message", true);
    }

    public boolean shouldNotifySender() {
        return config.getBoolean("actions.notify-sender", true);
    }

    public boolean isAuditLogEnabled() {
        return config.getBoolean("actions.audit-log", true);
    }

    public boolean isDiscordLogEnabled() {
        return config.getBoolean("actions.discord-log", false);
    }

    public boolean includeBlockedContentInAudit() {
        return config.getBoolean("actions.include-blocked-content-in-audit", true);
    }

    public String getBypassPermission() {
        return config.getString("permissions.bypass", "ezchat.bypass.flood");
    }

    public String getFeatureDisabledMessage() {
        return config.getString("messages.feature-disabled", "&cThat feature is currently disabled.");
    }

    public String getCooldownMessage() {
        return config.getString("messages.cooldown", "&cYou are sending messages too quickly.");
    }

    public String getDuplicateMessage() {
        return config.getString("messages.duplicate", "&cDo not repeat the same message.");
    }

    public String getBurstMessage() {
        return config.getString("messages.burst", "&cYou are sending too many messages too quickly.");
    }
}
