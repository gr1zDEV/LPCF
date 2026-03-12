package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.DeathMessageConfig;
import com.ezinnovations.ezchat.service.DeathMessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ToggleDeathMessageCommand implements CommandExecutor {

    private final EzChat plugin;
    private final DeathMessageConfig deathMessageConfig;
    private final DeathMessageService deathMessageService;

    public ToggleDeathMessageCommand(final EzChat plugin,
                                     final DeathMessageConfig deathMessageConfig,
                                     final DeathMessageService deathMessageService) {
        this.plugin = plugin;
        this.deathMessageConfig = deathMessageConfig;
        this.deathMessageService = deathMessageService;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("players-only", "&cOnly players can use this command.")));
            return true;
        }

        if (!deathMessageConfig.isFeatureEnabled()) {
            player.sendMessage(plugin.colorize(deathMessageConfig.getMessage("feature-disabled", "&cThat feature is currently disabled.")));
            return true;
        }

        if (!player.hasPermission("ezchat.deathmsg.toggle")) {
            player.sendMessage(plugin.colorize(deathMessageConfig.getMessage("no-permission", "&cYou do not have permission.")));
            return true;
        }

        final ToggleMode mode = ToggleModeParser.parse(args);
        if (mode == null) {
            player.sendMessage(plugin.colorize(deathMessageConfig.getMessage("toggle-invalid-usage", "&cUsage: /toggledeathmsg [on|off]")));
            return true;
        }

        if (mode == ToggleMode.TOGGLE) {
            final boolean enabled = deathMessageService.toggleDeathMessages(player.getUniqueId());
            player.sendMessage(plugin.colorize(deathMessageConfig.getMessage(
                    enabled ? "toggle-enabled" : "toggle-disabled",
                    enabled ? "&aYou can now receive death messages." : "&cYou will no longer receive death messages."
            )));
            return true;
        }

        final boolean enableRequested = mode == ToggleMode.ON;
        final boolean currentlyEnabled = deathMessageService.isDeathMessagesEnabled(player.getUniqueId());
        if (enableRequested == currentlyEnabled) {
            player.sendMessage(plugin.colorize(deathMessageConfig.getMessage(
                    enableRequested ? "toggle-already-enabled" : "toggle-already-disabled",
                    enableRequested ? "&eDeath messages are already enabled." : "&eDeath messages are already disabled."
            )));
            return true;
        }

        deathMessageService.setDeathMessagesEnabled(player.getUniqueId(), enableRequested);
        player.sendMessage(plugin.colorize(deathMessageConfig.getMessage(
                enableRequested ? "toggle-enabled" : "toggle-disabled",
                enableRequested ? "&aYou can now receive death messages." : "&cYou will no longer receive death messages."
        )));
        return true;
    }
}
