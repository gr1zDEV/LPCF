package com.ezinnovations.ezchat.commands;

import com.ezinnovations.ezchat.EzChat;
import com.ezinnovations.ezchat.managers.IgnoreManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class IgnoreCommand implements CommandExecutor {

    private final EzChat plugin;
    private final IgnoreManager ignoreManager;

    public IgnoreCommand(final EzChat plugin, final IgnoreManager ignoreManager) {
        this.plugin = plugin;
        this.ignoreManager = ignoreManager;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(plugin.colorize(plugin.getConfig().getString("players-only", "&cOnly players can use this command.")));
            return true;
        }

        if (!player.hasPermission("ezchat.ignore")) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.no-permission", "&cYou do not have permission.")));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(plugin.colorize("&cUsage: /ignore <player> <ALL|CHAT|MSG>"));
            return true;
        }

        final Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.player-not-found", "&cPlayer not found.")));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.cannot-ignore-self", "&cYou cannot ignore yourself.")));
            return true;
        }

        final IgnoreManager.IgnoreType ignoreType;
        try {
            ignoreType = IgnoreManager.IgnoreType.valueOf(args[1].toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException exception) {
            player.sendMessage(plugin.colorize("&cUsage: /ignore <player> <ALL|CHAT|MSG>"));
            return true;
        }

        final boolean enabled = ignoreManager.toggleIgnore(player.getUniqueId(), target.getUniqueId(), ignoreType);
        ignoreManager.save();

        if (!enabled) {
            player.sendMessage(plugin.colorize(plugin.getConfig().getString("ignore.disabled", "&aYou are no longer ignoring {player}.")
                    .replace("{player}", target.getName())));
            return true;
        }

        final String key;
        switch (ignoreType) {
            case CHAT -> key = "ignore.enabled-chat";
            case MSG -> key = "ignore.enabled-msg";
            default -> key = "ignore.enabled-all";
        }

        player.sendMessage(plugin.colorize(plugin.getConfig().getString(key, "&cYou are now ignoring {player}.")
                .replace("{player}", target.getName())));
        return true;
    }
}
