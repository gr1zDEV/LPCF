package com.ezinnovations.ezchat.config;

import com.ezinnovations.ezchat.EzChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class StaffConfig {

    private final EzChat plugin;
    private File file;
    private FileConfiguration config;

    public StaffConfig(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        file = new File(plugin.getDataFolder(), "staff.yml");
        if (!file.exists()) {
            plugin.saveResource("staff.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public boolean isStaffChatEnabled() {
        return config.getBoolean("features.staff-chat.enabled", true);
    }

    public boolean isStaffAlertsEnabled() {
        return config.getBoolean("features.staff-alerts.enabled", true);
    }

    public boolean isConsoleStaffAlertCommandEnabled() {
        return config.getBoolean("features.console-staff-alert-command.enabled", true);
    }

    public boolean shouldLogStaffChat() {
        return config.getBoolean("settings.log-staff-chat", true);
    }

    public boolean shouldLogStaffAlerts() {
        return config.getBoolean("settings.log-staff-alerts", true);
    }

    public String getFormat(final String path, final String fallback) {
        return config.getString("formats." + path, fallback);
    }

    public String getMessage(final String path, final String fallback) {
        return config.getString("messages." + path, fallback);
    }
}
