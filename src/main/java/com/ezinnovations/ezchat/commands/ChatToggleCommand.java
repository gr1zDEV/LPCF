package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.managers.ChatToggleManager;
import com.ezinnovations.ezchat.managers.FeatureManager;
import com.ezinnovations.ezchat.service.AuditLogService;
import com.ezinnovations.ezchat.service.DiscordNotificationService;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ChatToggleCommand implements CommandExecutor {

    private final EzChat plugin;
    private final FeatureManager featureManager;
    private final ChatToggleManager chatToggleManager;
    private final AuditLogService auditLogService;
    private final DiscordNotificationService discordNotificationService;

    public ChatToggleCommand(final EzChat plugin,
                             final FeatureManager featureManager,
                             final ChatToggleManager chatToggleManager,
                             final AuditLogService auditLogService,
                             final DiscordNotificationService discordNotificationService) {
        this.plugin = plugin;
        this.featureManager = featureManager;
        this.chatToggleManager = chatToggleManager;
        this.auditLogService = auditLogService;
        this.discordNotificationService = discordNotificationService;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("players-only", "&cOnly players can use this command.")));
            return true;
        }

        if (!featureManager.isChatToggleEnabled()) {
            player.sendMessage(plugin.colorize(featureManager.getFeatureDisabledMessage()));
            return true;
        }

        if (!player.hasPermission("ezchat.chattoggle")) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("no-permission", "&cYou do not have permission.")));
            return true;
        }

        final ToggleMode mode = ToggleModeParser.parse(args);
        if (mode == null) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("chat-toggle.invalid-usage", "&cUsage: /togglechat [on|off]")));
            return true;
        }

        if (mode == ToggleMode.TOGGLE) {
            final boolean nowHidden = chatToggleManager.toggleChat(player.getUniqueId());
            auditLogService.log(player, "TOGGLE_CHAT", "set public chat visibility " + (nowHidden ? "OFF" : "ON"));
            discordNotificationService.sendAuditAction(player.getUniqueId(), player.getName(), "set public chat visibility " + (nowHidden ? "OFF" : "ON"));
            if (nowHidden) {
                player.sendMessage(plugin.colorize(plugin.getConfig().getString("chat-toggle.disabled", "&cPublic chat is now hidden.")));
            } else {
                player.sendMessage(plugin.colorize(plugin.getConfig().getString("chat-toggle.enabled", "&aPublic chat is now visible.")));
            }
            return true;
        }

        final boolean shouldHide = mode == ToggleMode.OFF;
        final boolean currentlyHidden = chatToggleManager.isChatHidden(player.getUniqueId());
        if (shouldHide == currentlyHidden) {
            if (shouldHide) {
                player.sendMessage(plugin.colorize(plugin.getConfig().getString("chat-toggle.already-disabled", "&ePublic chat is already hidden.")));
            } else {
                player.sendMessage(plugin.colorize(plugin.getConfig().getString("chat-toggle.already-enabled", "&ePublic chat is already visible.")));
            }
            return true;
        }

        chatToggleManager.setChatHidden(player.getUniqueId(), shouldHide);
        auditLogService.log(player, "TOGGLE_CHAT", "set public chat visibility " + (shouldHide ? "OFF" : "ON"));
        discordNotificationService.sendAuditAction(player.getUniqueId(), player.getName(), "set public chat visibility " + (shouldHide ? "OFF" : "ON"));
        if (shouldHide) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("chat-toggle.disabled", "&cPublic chat is now hidden.")));
        } else {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("chat-toggle.enabled", "&aPublic chat is now visible.")));
        }

        return true;
    }
}
