package com.ezinnovations.ezchat.managers;

import com.ezinnovations.ezchat.EzChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class ConfigManager {

    private final EzChat plugin;

    private File privateMessageFile;
    private FileConfiguration privateMessageConfig;

    private File mailFile;
    private FileConfiguration mailConfig;

    public ConfigManager(final EzChat plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("[EzChat] Unable to create plugin data folder.");
        }

        privateMessageFile = new File(plugin.getDataFolder(), "private-message.yml");
        if (!privateMessageFile.exists()) {
            plugin.saveResource("private-message.yml", false);
        }
        privateMessageConfig = YamlConfiguration.loadConfiguration(privateMessageFile);

        mailFile = new File(plugin.getDataFolder(), "mail.yml");
        if (!mailFile.exists()) {
            plugin.saveResource("mail.yml", false);
        }
        mailConfig = YamlConfiguration.loadConfiguration(mailFile);
    }

    public FileConfiguration getPrivateMessageConfig() {
        return privateMessageConfig;
    }

    public FileConfiguration getMailConfig() {
        return mailConfig;
    }
}
