package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.managers.ChatToggleManager;
import com.ezinnovations.ezchat.managers.FeatureManager;
import com.ezinnovations.ezchat.service.AuditLogService;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ToggleMailCommand implements CommandExecutor {

    private final EzChat plugin;
    private final FeatureManager featureManager;
    private final ChatToggleManager chatToggleManager;
    private final AuditLogService auditLogService;

    public ToggleMailCommand(final EzChat plugin,
                             final FeatureManager featureManager,
                             final ChatToggleManager chatToggleManager,
                             final AuditLogService auditLogService) {
        this.plugin = plugin;
        this.featureManager = featureManager;
        this.chatToggleManager = chatToggleManager;
        this.auditLogService = auditLogService;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("players-only", "&cOnly players can use this command.")));
            return true;
        }

        if (!featureManager.isMailToggleEnabled()) {
            player.sendMessage(plugin.colorize(featureManager.getFeatureDisabledMessage()));
            return true;
        }

        if (!player.hasPermission("ezchat.togglemail")) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.no-permission", "&cYou do not have permission.")));
            return true;
        }

        final ToggleMode mode = ToggleModeParser.parse(args);
        if (mode == null) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.toggle-invalid-usage", "&cUsage: /togglemail [on|off]")));
            return true;
        }

        if (mode == ToggleMode.TOGGLE) {
            final boolean nowDisabled = chatToggleManager.toggleMail(player.getUniqueId());
            auditLogService.log(player, "TOGGLE_MAIL", "set mail " + (nowDisabled ? "OFF" : "ON"));
            if (nowDisabled) {
                player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.toggle-disabled", "&cYou will no longer receive mail.")));
            } else {
                player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.toggle-enabled", "&aYou can now receive mail.")));
            }
            return true;
        }

        final boolean shouldDisable = mode == ToggleMode.OFF;
        final boolean currentlyDisabled = chatToggleManager.isMailDisabled(player.getUniqueId());
        if (shouldDisable == currentlyDisabled) {
            if (shouldDisable) {
                player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.toggle-already-disabled", "&eMail is already disabled.")));
            } else {
                player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.toggle-already-enabled", "&eMail is already enabled.")));
            }
            return true;
        }

        chatToggleManager.setMailDisabled(player.getUniqueId(), shouldDisable);
        auditLogService.log(player, "TOGGLE_MAIL", "set mail " + (shouldDisable ? "OFF" : "ON"));
        if (shouldDisable) {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.toggle-disabled", "&cYou will no longer receive mail.")));
        } else {
            player.sendMessage(plugin.colorize(plugin.getConfigManager().getMailConfig().getString("messages.toggle-enabled", "&aYou can now receive mail.")));
        }
        return true;
    }
}
