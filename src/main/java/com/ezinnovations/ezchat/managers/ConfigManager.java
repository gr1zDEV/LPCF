package com.ezinnovations.ezchat.managers;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.DiscordConfig;
import com.ezinnovations.ezchat.config.LogsConfig;
import com.ezinnovations.ezchat.config.MuteConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class ConfigManager {

    private final EzChat plugin;

    private File privateMessageFile;
    private FileConfiguration privateMessageConfig;

    private File mailFile;
    private FileConfiguration mailConfig;

    private final LogsConfig logsConfig;
    private final MuteConfig muteConfig;
    private final DiscordConfig discordConfig;

    public ConfigManager(final EzChat plugin) {
        this.plugin = plugin;
        this.logsConfig = new LogsConfig(plugin);
        this.muteConfig = new MuteConfig(plugin);
        this.discordConfig = new DiscordConfig(plugin);
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

        logsConfig.reload();
        muteConfig.reload();
        discordConfig.reload();
    }

    public FileConfiguration getPrivateMessageConfig() {
        return privateMessageConfig;
    }

    public FileConfiguration getMailConfig() {
        return mailConfig;
    }

    public LogsConfig getLogsConfig() {
        return logsConfig;
    }

    public MuteConfig getMuteConfig() {
        return muteConfig;
    }

    public DiscordConfig getDiscordConfig() {
        return discordConfig;
    }
}
