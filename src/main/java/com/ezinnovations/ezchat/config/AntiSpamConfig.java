package com.ezinnovations.ezchat.config;

import com.ezinnovations.ezchat.EzChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class AntiSpamConfig {

    private final EzChat plugin;

    private File file;
    private FileConfiguration config;

    public AntiSpamConfig(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "anti-spam.yml");
        if (!file.exists()) {
            plugin.saveResource("anti-spam.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isFeatureEnabled() {
        return config.getBoolean("features.enabled", true)
                && config.getBoolean("features.anti-advertising.enabled", true);
    }

    public boolean shouldScanPublicChat() {
        return config.getBoolean("checks.public-chat", true);
    }

    public boolean shouldScanPrivateMessages() {
        return config.getBoolean("checks.private-messages", true);
    }

    public boolean shouldScanMail() {
        return config.getBoolean("checks.mail", false);
    }

    public boolean shouldBlockIpv4() {
        return config.getBoolean("patterns.block-ipv4", true);
    }

    public boolean shouldBlockDomains() {
        return config.getBoolean("patterns.block-domains", true);
    }

    public boolean shouldBlockDiscordInvites() {
        return config.getBoolean("patterns.block-discord-invites", true);
    }

    public List<String> getCustomBlockedPatterns() {
        return filterEmpty(config.getStringList("patterns.custom-blocked"));
    }

    public List<String> getWhitelistedDomains() {
        return filterEmpty(config.getStringList("whitelist.domains"));
    }

    public List<String> getWhitelistedExact() {
        return filterEmpty(config.getStringList("whitelist.exact"));
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
        return config.getString("permissions.bypass", "ezchat.bypass.advertising");
    }

    public String getBlockedMessage() {
        return config.getString("messages.blocked", "&cAdvertising is not allowed.");
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
