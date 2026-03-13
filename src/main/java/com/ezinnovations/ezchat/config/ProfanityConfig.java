package com.ezinnovations.ezchat.config;

import com.ezinnovations.ezchat.EzChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ProfanityConfig {

    private final EzChat plugin;

    private File file;
    private FileConfiguration config;

    public ProfanityConfig(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "profanity.yml");
        if (!file.exists()) {
            plugin.saveResource("profanity.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isFeatureEnabled() {
        return config.getBoolean("features.enabled", true);
    }

    public boolean shouldScanPublicChat() {
        return config.getBoolean("checks.public-chat", true);
    }

    public boolean shouldScanPrivateMessages() {
        return config.getBoolean("checks.private-messages", true);
    }

    public boolean shouldScanMail() {
        return config.getBoolean("checks.mail", true);
    }

    public boolean shouldScanStaffChat() {
        return config.getBoolean("checks.staff-chat", false);
    }

    public boolean isWordListEnabled() {
        return config.getBoolean("word-list.enabled", true);
    }

    public List<String> getBlockedWords() {
        return filterEmpty(config.getStringList("word-list.blocked"));
    }

    public boolean isRegexEnabled() {
        return config.getBoolean("regex.enabled", true);
    }

    public List<String> getRegexPatterns() {
        return filterEmpty(config.getStringList("regex.patterns"));
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
        return config.getString("permissions.bypass", "ezchat.bypass.profanity");
    }

    public String getBlockedMessage() {
        return config.getString("messages.blocked", "&cThat message contains blocked language.");
    }

    public String getFeatureDisabledMessage() {
        return config.getString("messages.feature-disabled", "&cThat feature is currently disabled.");
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
