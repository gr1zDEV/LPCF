package com.ezinnovations.ezchat.managers;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.AntiSpamConfig;
import com.ezinnovations.ezchat.config.BlockedWordsConfig;
import com.ezinnovations.ezchat.config.DeathMessageConfig;
import com.ezinnovations.ezchat.config.DiscordConfig;
import com.ezinnovations.ezchat.config.LogsConfig;
import com.ezinnovations.ezchat.config.JoinLeaveConfig;
import com.ezinnovations.ezchat.config.MuteConfig;
import com.ezinnovations.ezchat.config.ProfanityConfig;
import com.ezinnovations.ezchat.config.StaffConfig;
import com.ezinnovations.ezchat.config.ServerMessageConfig;
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
    private final AntiSpamConfig antiSpamConfig;
    private final ProfanityConfig profanityConfig;
    private final BlockedWordsConfig blockedWordsConfig;
    private final StaffConfig staffConfig;
    private final ServerMessageConfig serverMessageConfig;
    private final DeathMessageConfig deathMessageConfig;
    private final JoinLeaveConfig joinLeaveConfig;

    public ConfigManager(final EzChat plugin) {
        this.plugin = plugin;
        this.logsConfig = new LogsConfig(plugin);
        this.muteConfig = new MuteConfig(plugin);
        this.discordConfig = new DiscordConfig(plugin);
        this.antiSpamConfig = new AntiSpamConfig(plugin);
        this.profanityConfig = new ProfanityConfig(plugin);
        this.blockedWordsConfig = new BlockedWordsConfig(plugin);
        this.staffConfig = new StaffConfig(plugin);
        this.serverMessageConfig = new ServerMessageConfig(plugin);
        this.deathMessageConfig = new DeathMessageConfig(plugin);
        this.joinLeaveConfig = new JoinLeaveConfig(plugin);
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
        antiSpamConfig.reload();
        profanityConfig.reload();
        blockedWordsConfig.reload();
        staffConfig.reload();
        serverMessageConfig.reload();
        deathMessageConfig.reload();
        joinLeaveConfig.reload();
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

    public AntiSpamConfig getAntiSpamConfig() {
        return antiSpamConfig;
    }

    public ProfanityConfig getProfanityConfig() {
        return profanityConfig;
    }

    public BlockedWordsConfig getBlockedWordsConfig() {
        return blockedWordsConfig;
    }

    public StaffConfig getStaffConfig() {
        return staffConfig;
    }

    public ServerMessageConfig getServerMessageConfig() {
        return serverMessageConfig;
    }

    public DeathMessageConfig getDeathMessageConfig() {
        return deathMessageConfig;
    }

    public JoinLeaveConfig getJoinLeaveConfig() {
        return joinLeaveConfig;
    }
}
