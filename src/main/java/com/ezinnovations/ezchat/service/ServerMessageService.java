package com.ezinnovations.ezchat.service;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.ServerMessageConfig;
import com.ezinnovations.ezchat.managers.ChatToggleManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ServerMessageService {

    private final EzChat plugin;
    private final ServerMessageConfig serverMessageConfig;
    private final ChatToggleManager chatToggleManager;
    private final CommunicationLogService communicationLogService;
    private final AuditLogService auditLogService;
    private final DiscordNotificationService discordNotificationService;

    public ServerMessageService(final EzChat plugin,
                                final ServerMessageConfig serverMessageConfig,
                                final ChatToggleManager chatToggleManager,
                                final CommunicationLogService communicationLogService,
                                final AuditLogService auditLogService,
                                final DiscordNotificationService discordNotificationService) {
        this.plugin = plugin;
        this.serverMessageConfig = serverMessageConfig;
        this.chatToggleManager = chatToggleManager;
        this.communicationLogService = communicationLogService;
        this.auditLogService = auditLogService;
        this.discordNotificationService = discordNotificationService;
    }

    public boolean isFeatureEnabled() {
        return serverMessageConfig.isFeatureEnabled();
    }

    public boolean isServerMessagesEnabled(final UUID playerUuid) {
        return !chatToggleManager.areServerMessagesDisabled(playerUuid);
    }

    public boolean setServerMessagesEnabled(final UUID playerUuid, final boolean enabled) {
        return chatToggleManager.setServerMessagesDisabled(playerUuid, !enabled);
    }

    public boolean toggleServerMessages(final UUID playerUuid) {
        final boolean nowDisabled = chatToggleManager.toggleServerMessages(playerUuid);
        return !nowDisabled;
    }

    public void sendBroadcast(final CommandSender sender, final String message) {
        final String rendered = plugin.colorize(resolveFormat(sender)
                .replace("{sender}", sender.getName())
                .replace("{message}", message));

        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            for (final Player online : plugin.getServer().getOnlinePlayers()) {
                if (isServerMessagesEnabled(online.getUniqueId())) {
                    online.sendMessage(rendered);
                }
            }
        });

        if (serverMessageConfig.shouldLogBroadcasts()) {
            if (sender instanceof final Player playerSender) {
                communicationLogService.logBroadcast(playerSender.getUniqueId(), playerSender.getName(), message);
            } else {
                communicationLogService.logBroadcast(null, sender.getName(), message);
            }
        }

        discordNotificationService.sendServerBroadcast(sender, message);
    }

    public void logToggleAction(final Player player, final boolean enabled) {
        if (!serverMessageConfig.shouldLogToggleActions()) {
            return;
        }

        final String details = "set server messages " + (enabled ? "ON" : "OFF");
        auditLogService.log(player, "TOGGLE_SERVERMSG", details);
        discordNotificationService.sendAuditAction(player.getUniqueId(), player.getName(), details);
    }

    private String resolveFormat(final CommandSender sender) {
        if (sender instanceof Player) {
            return serverMessageConfig.getFormat("player-broadcast", "&8[&6Server&8] &7[{sender}]&f {message}");
        }
        return serverMessageConfig.getFormat("console-broadcast", "&8[&6Server&8] &7[Console]&f {message}");
    }
}
