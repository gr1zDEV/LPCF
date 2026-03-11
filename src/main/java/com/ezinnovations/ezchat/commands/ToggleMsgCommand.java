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

public final class ToggleMsgCommand implements CommandExecutor {

    private final EzChat plugin;
    private final FeatureManager featureManager;
    private final ChatToggleManager chatToggleManager;
    private final AuditLogService auditLogService;
    private final DiscordNotificationService discordNotificationService;

    public ToggleMsgCommand(final EzChat plugin,
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

        if (!featureManager.isPrivateMessageToggleEnabled()) {
            player.sendMessage(plugin.colorize(featureManager.getFeatureDisabledMessage()));
            return true;
        }

        if (!player.hasPermission("ezchat.togglemsg")) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getPrivateMessageConfig().getString("messages.no-permission", "&cYou do not have permission.")));
            return true;
        }

        final ToggleMode mode = ToggleModeParser.parse(args);
        if (mode == null) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getPrivateMessageConfig().getString("messages.toggle-invalid-usage", "&cUsage: /togglemsg [on|off]")));
            return true;
        }

        if (mode == ToggleMode.TOGGLE) {
            final boolean nowDisabled = chatToggleManager.togglePrivateMessages(player.getUniqueId());
            auditLogService.log(player, "TOGGLE_MSG", "set private messages " + (nowDisabled ? "OFF" : "ON"));
            discordNotificationService.sendAuditAction(player.getUniqueId(), player.getName(), "set private messages " + (nowDisabled ? "OFF" : "ON"));
            if (nowDisabled) {
                player.sendMessage(plugin.colorize(plugin.getConfigManager().getPrivateMessageConfig().getString("messages.toggle-disabled", "&cYou will no longer receive private messages.")));
            } else {
                player.sendMessage(plugin.colorize(plugin.getConfigManager().getPrivateMessageConfig().getString("messages.toggle-enabled", "&aYou can now receive private messages.")));
            }
            return true;
        }

        final boolean shouldDisable = mode == ToggleMode.OFF;
        final boolean currentlyDisabled = chatToggleManager.arePrivateMessagesDisabled(player.getUniqueId());
        if (shouldDisable == currentlyDisabled) {
            if (shouldDisable) {
                player.sendMessage(plugin.colorize(plugin.getConfigManager().getPrivateMessageConfig().getString("messages.toggle-already-disabled", "&ePrivate messages are already disabled.")));
            } else {
                player.sendMessage(plugin.colorize(plugin.getConfigManager().getPrivateMessageConfig().getString("messages.toggle-already-enabled", "&ePrivate messages are already enabled.")));
            }
            return true;
        }

        chatToggleManager.setPrivateMessagesDisabled(player.getUniqueId(), shouldDisable);
        auditLogService.log(player, "TOGGLE_MSG", "set private messages " + (shouldDisable ? "OFF" : "ON"));
        discordNotificationService.sendAuditAction(player.getUniqueId(), player.getName(), "set private messages " + (shouldDisable ? "OFF" : "ON"));
        if (shouldDisable) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getPrivateMessageConfig().getString("messages.toggle-disabled", "&cYou will no longer receive private messages.")));
        } else {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getPrivateMessageConfig().getString("messages.toggle-enabled", "&aYou can now receive private messages.")));
        }

        return true;
    }
}
