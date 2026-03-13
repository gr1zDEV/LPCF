package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.JoinLeaveConfig;
import com.ezinnovations.ezchat.service.JoinLeaveService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ToggleJoinLeaveMessageCommand implements CommandExecutor {

    private final EzChat plugin;
    private final JoinLeaveConfig joinLeaveConfig;
    private final JoinLeaveService joinLeaveService;

    public ToggleJoinLeaveMessageCommand(final EzChat plugin,
                                         final JoinLeaveConfig joinLeaveConfig,
                                         final JoinLeaveService joinLeaveService) {
        this.plugin = plugin;
        this.joinLeaveConfig = joinLeaveConfig;
        this.joinLeaveService = joinLeaveService;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("players-only", "&cOnly players can use this command.")));
            return true;
        }

        if (!joinLeaveConfig.isFeatureEnabled()) {
            player.sendMessage(plugin.colorize(joinLeaveConfig.getMessage("feature-disabled", "&cThat feature is currently disabled.")));
            return true;
        }

        if (!player.hasPermission("ezchat.joinleavemsg.toggle")) {
            player.sendMessage(plugin.colorize(joinLeaveConfig.getMessage("no-permission", "&cYou do not have permission.")));
            return true;
        }

        final ToggleMode mode = ToggleModeParser.parse(args);
        if (mode == null) {
            player.sendMessage(plugin.colorize(joinLeaveConfig.getMessage("toggle-invalid-usage", "&cUsage: /togglejoinleavemsg [on|off]")));
            return true;
        }

        if (mode == ToggleMode.TOGGLE) {
            final boolean enabled = joinLeaveService.toggleJoinLeaveMessages(player.getUniqueId());
            player.sendMessage(plugin.colorize(joinLeaveConfig.getMessage(
                    enabled ? "toggle-enabled" : "toggle-disabled",
                    enabled ? "&aYou can now receive join/leave messages." : "&cYou will no longer receive join/leave messages."
            )));
            return true;
        }

        final boolean enableRequested = mode == ToggleMode.ON;
        final boolean currentlyEnabled = joinLeaveService.isJoinLeaveMessagesEnabled(player.getUniqueId());
        if (enableRequested == currentlyEnabled) {
            player.sendMessage(plugin.colorize(joinLeaveConfig.getMessage(
                    enableRequested ? "toggle-already-enabled" : "toggle-already-disabled",
                    enableRequested ? "&eJoin/leave messages are already enabled." : "&eJoin/leave messages are already disabled."
            )));
            return true;
        }

        joinLeaveService.setJoinLeaveMessagesEnabled(player.getUniqueId(), enableRequested);
        player.sendMessage(plugin.colorize(joinLeaveConfig.getMessage(
                enableRequested ? "toggle-enabled" : "toggle-disabled",
                enableRequested ? "&aYou can now receive join/leave messages." : "&cYou will no longer receive join/leave messages."
        )));
        return true;
    }
}
