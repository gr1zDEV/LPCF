package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.config.ServerMessageConfig;
import com.ezinnovations.ezchat.service.ServerMessageService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public final class BroadcastSubcommand {

    private final EzChat plugin;
    private final ServerMessageConfig serverMessageConfig;
    private final ServerMessageService serverMessageService;

    public BroadcastSubcommand(final EzChat plugin,
                               final ServerMessageConfig serverMessageConfig,
                               final ServerMessageService serverMessageService) {
        this.plugin = plugin;
        this.serverMessageConfig = serverMessageConfig;
        this.serverMessageService = serverMessageService;
    }

    public boolean execute(final CommandSender sender, final String[] args) {
        if (!serverMessageConfig.isFeatureEnabled()) {
            sender.sendMessage(plugin.colorize(serverMessageConfig.getMessage("feature-disabled", "&cThat feature is currently disabled.")));
            return true;
        }

        if (sender instanceof Player && !sender.hasPermission("ezchat.broadcast")) {
            sender.sendMessage(plugin.colorize(serverMessageConfig.getMessage("no-permission", "&cYou do not have permission.")));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.colorize(serverMessageConfig.getMessage("broadcast-invalid-usage", "&cUsage: /ezchat broadcast <message>")));
            return true;
        }

        final String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if (message.isEmpty()) {
            sender.sendMessage(plugin.colorize(serverMessageConfig.getMessage("broadcast-invalid-usage", "&cUsage: /ezchat broadcast <message>")));
            return true;
        }

        serverMessageService.sendBroadcast(sender, message);
        return true;
    }

    public List<String> tabComplete(final CommandSender sender, final String[] args) {
        if (args.length == 1 && (sender.hasPermission("ezchat.broadcast") || !(sender instanceof Player))) {
            return List.of("broadcast").stream().filter(v -> v.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}
