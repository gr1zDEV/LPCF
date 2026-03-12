package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.ServerMessageConfig;
import com.ezinnovations.ezchat.service.ServerMessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ToggleServerMessageCommand implements CommandExecutor {

    private final EzChat plugin;
    private final ServerMessageConfig serverMessageConfig;
    private final ServerMessageService serverMessageService;

    public ToggleServerMessageCommand(final EzChat plugin,
                                      final ServerMessageConfig serverMessageConfig,
                                      final ServerMessageService serverMessageService) {
        this.plugin = plugin;
        this.serverMessageConfig = serverMessageConfig;
        this.serverMessageService = serverMessageService;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("players-only", "&cOnly players can use this command.")));
            return true;
        }

        if (!serverMessageConfig.isFeatureEnabled()) {
            player.sendMessage(plugin.colorize(serverMessageConfig.getMessage("feature-disabled", "&cThat feature is currently disabled.")));
            return true;
        }

        if (!player.hasPermission("ezchat.servermsg.toggle")) {
            player.sendMessage(plugin.colorize(serverMessageConfig.getMessage("no-permission", "&cYou do not have permission.")));
            return true;
        }

        final ToggleMode mode = ToggleModeParser.parse(args);
        if (mode == null) {
            player.sendMessage(plugin.colorize(serverMessageConfig.getMessage("toggle-invalid-usage", "&cUsage: /toggleservermsg [on|off]")));
            return true;
        }

        if (mode == ToggleMode.TOGGLE) {
            final boolean enabled = serverMessageService.toggleServerMessages(player.getUniqueId());
            serverMessageService.logToggleAction(player, enabled);
            player.sendMessage(plugin.colorize(serverMessageConfig.getMessage(
                    enabled ? "toggle-enabled" : "toggle-disabled",
                    enabled ? "&aYou can now receive server messages." : "&cYou will no longer receive server messages."
            )));
            return true;
        }

        final boolean enableRequested = mode == ToggleMode.ON;
        final boolean currentlyEnabled = serverMessageService.isServerMessagesEnabled(player.getUniqueId());
        if (enableRequested == currentlyEnabled) {
            player.sendMessage(plugin.colorize(serverMessageConfig.getMessage(
                    enableRequested ? "toggle-already-enabled" : "toggle-already-disabled",
                    enableRequested ? "&eServer messages are already enabled." : "&eServer messages are already disabled."
            )));
            return true;
        }

        serverMessageService.setServerMessagesEnabled(player.getUniqueId(), enableRequested);
        serverMessageService.logToggleAction(player, enableRequested);
        player.sendMessage(plugin.colorize(serverMessageConfig.getMessage(
                enableRequested ? "toggle-enabled" : "toggle-disabled",
                enableRequested ? "&aYou can now receive server messages." : "&cYou will no longer receive server messages."
        )));
        return true;
    }
}
